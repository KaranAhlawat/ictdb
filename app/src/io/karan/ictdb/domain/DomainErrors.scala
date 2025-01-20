package io.karan.ictdb.domain

import scala.util.control.NoStackTrace

enum DomainErrors extends NoStackTrace:
  case InvalidCredentials(msg: String)
