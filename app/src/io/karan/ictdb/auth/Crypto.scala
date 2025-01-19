package io.karan.ictdb.auth

import cats.effect.IO
import com.google.crypto.tink.Aead
import com.google.crypto.tink.subtle.Base64

trait Crypto:
  def encrypt(plain: String): IO[String]

  def decrypt(encrypted: String): IO[String]

object Crypto:
  def make(aead: Aead): Crypto =
    new Crypto:
      def encrypt(plain: String): IO[String] =
        IO.delay(aead.encrypt(plain.getBytes(), null))
          .map(Base64.encode)

      def decrypt(encrypted: String): IO[String] =
        IO.delay(Base64.decode(encrypted))
          .map(decoded => String(aead.decrypt(decoded, null), "UTF-8"))
