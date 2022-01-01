package io.gitlab.mhammons.slinc

import jdk.incubator.foreign.SegmentAllocator

object Testlib extends Library(Location.Local("slinc/test/native/libtest.so")):
   case class a_t(a: Int, b: Int) derives Struct
   case class b_t(c: Int, d: a_t) derives Struct

   case class c_t(a: StaticArray[Int, 3], b: StaticArray[Float, 3])
       derives Struct

   def slinc_test_modify(b_t: b_t): b_t = bind
   def slinc_test_addone(c_t: c_t): c_t = bind
   def slinc_test_getstaticarr(): Ptr[Int] = bind
   def slinc_test_getstaticarr1(res: Ptr[Int]): Unit = bind
   def slinc_two_structs(a: a_t, b: a_t): Int = bind
