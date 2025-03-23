package io.karan.ictdb.http.auth

import cats.effect.IO
import org.http4s.{Request, RequestCookie}

extension (req: Request[IO])
  def checkAuthn[A](authenticated: RequestCookie => A)(unauthenticated: => A) =
    req.cookies.find(_.name == Cookies.AUTH).fold(unauthenticated)(authenticated)
