package io.karan.ictdb.http.auth

import cats.effect.*
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import com.nimbusds.oauth2.sdk.http.*
import com.nimbusds.oauth2.sdk.{Request as _, Response as _, *}
import org.http4s.*
import org.http4s.client.*
import org.typelevel.ci.CIString

import scala.jdk.CollectionConverters.*

case class Http4sRequestSender(client: Client[IO], dispatcher: Dispatcher[IO]) extends HTTPRequestSender:
  override def send(httpRequest: ReadOnlyHTTPRequest): ReadOnlyHTTPResponse =
    val reqOpt =
      for
        method <- Method.fromString(httpRequest.getMethod.toString).toOption
        uri    <- Uri.fromString(httpRequest.getURI.toString).toOption
        headers = httpRequest.getHeaderMap.asScala.foldLeft(Headers.empty) { case (acc, curHeader) =>
                    acc.put(Header.Raw(CIString(curHeader._1), curHeader._2.asScala.mkString(",")))
                  }
      yield Request[IO](method = method, uri = uri, headers = headers)

    val request =
      if httpRequest.getBody != null then
        for
          urlForm <- UrlForm
                       .decodeString(Charset.`UTF-8`)(httpRequest.getBody)
                       .toOption
          req     <- reqOpt
        yield req.withEntity(urlForm)
      else reqOpt

    val respIO =
      IO.fromOption(request)(new IllegalArgumentException("Failed to convert request"))
        .flatMap: req =>
          client
            .run(req)
            .use: resp =>
              resp
                .as[String]
                .map: body =>
                  val request = new HTTPResponse(resp.status.code)
                  request.setStatusMessage(resp.status.reason)
                  resp.headers.headers.foreach(header => request.setHeader(header.name.toString, header.value))
                  request.setBody(body)
                  request

    dispatcher.unsafeRunSync(respIO)
  end send
end Http4sRequestSender

object Http4sRequestSender:
  def make(client: Client[IO]): Resource[IO, Http4sRequestSender] =
    Dispatcher.parallel[IO](false).map(Http4sRequestSender(client, _))
