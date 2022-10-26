package fr.hammons.slinc

import scala.quoted.{ToExpr, Expr, Quotes}

opaque type Bytes = Long 

object Bytes:
  def apply(l: Long): Bytes = l

  extension (a: Bytes) 
    inline def +(b: Bytes): Bytes = a + b
    inline def *(i: Int): Bytes = a * i
    inline def toLong: Long = a 

  given ToExpr[Bytes] with 
    def apply(t: Bytes)(using Quotes) = ToExpr.LongToExpr[Long].apply(t)