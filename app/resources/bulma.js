"use strict";

function addDeleteBehavior($delete) {
      const $notification = $delete.parentNode;

      $delete.addEventListener("click", () => {
        $notification.parentNode?.removeChild($notification);
      });
}

document.addEventListener("DOMContentLoaded", () => {
  (document.querySelectorAll(".notification .delete") || []).forEach(addDeleteBehavior);
});

document.addEventListener("htmx:afterSwap", (details) => {
  (document.querySelectorAll(".notification .delete") || []).forEach(
    ($delete) => {
      addDeleteBehavior($delete)
    }
  );
});
