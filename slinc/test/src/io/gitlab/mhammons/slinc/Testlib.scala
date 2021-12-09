package io.gitlab.mhammons.slinc

import jdk.incubator.foreign.SegmentAllocator

object Testlib extends Library(Location.Local("slinc/test/native/libtest.so")):
   case class a_t(a: Int, b: Int) derives Struct
   case class b_t(c: Int, d: a_t) derives Struct

   def slinc_test_modify(b_t: b_t)(using SegmentAllocator): b_t = bind
