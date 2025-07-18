import { router } from '@inertiajs/react';
import { clsx, type ClassValue } from 'clsx';
import { FieldValues, Path, UseFormReturn } from 'react-hook-form';
import { twMerge } from 'tailwind-merge';
import * as z from 'zod/v4-mini';

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export function postInertiaForm<T extends FieldValues, U extends UseFormReturn<T>, E extends string & Path<T>>(to: string, form: U) {
    return async (values: T) => {
        const errSchema = z.enum(Object.keys(values) as [E, ...E[]]);

        await new Promise<void>((resolve, reject) => {
            router.post(to, values, {
                onFinish: () => resolve(),
                onError: (e) => {
                    for (const key in e) {
                        const valid = errSchema.safeParse(key);
                        if (valid.success) {
                            form.setError(valid.data, {
                                message: e[valid.data],
                                type: 'value',
                            });
                        }
                    }
                    reject();
                },
            });
        });
    };
}

export const debounce = <F extends (...args: Parameters<F>) => ReturnType<F>>(func: F, waitFor: number) => {
    let timeout: ReturnType<typeof setTimeout>;

    const debounced = (...args: Parameters<F>) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), waitFor);
    };

    return debounced;
};
