package io.karan.ictdb.http.validation

import scala.util.control.NoStackTrace

enum ValidationError extends NoStackTrace:
  case MissingFields(fieldName: String)
  case PasswordMismatch
