package io.karan.ictdb.http

import cats.effect.IO
import io.karan.ictdb.auth.Crypto
import io.karan.ictdb.http.auth.checkAuthentication
import io.karan.ictdb.views.Layout
import scalatags.Text.all.*
import scalatags.Text.tags2.main
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.scalatags.*

object RootController:
  def routes(crypto: Crypto) = HttpRoutes.of[IO]:
    case req @ GET -> Root             =>
      req
        .checkAuthentication(c =>
          crypto.decrypt(c.content).map(decrypted => main(decrypted)).flatMap(c => Ok(Layout(true, c)))
        )(Ok(Layout(false, "Please login")))
