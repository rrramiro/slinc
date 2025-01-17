package fr.hammons.slinc

import scala.util.TupledFunction
import scala.annotation.experimental
import scala.annotation.targetName
import scala.compiletime.{erasedValue, summonInline}
import scala.reflect.ClassTag

class Ptr[A](private[slinc] val mem: Mem, private[slinc] val offset: Bytes):
  def `unary_!`(using receive: Receive[A]) = receive.from(mem, offset)
  def asArray(size: Int)(using
      ClassTag[A]
  )(using l: LayoutOf[A], r: ReceiveBulk[A]) =
    r.from(mem.resize(Bytes(l.layout.size.toLong * size)), offset, size)

  def `unary_!_=`(value: A)(using send: Send[A]) = send.to(mem, offset, value)
  def apply(bytes: Bytes) = Ptr[A](mem, offset + bytes)
  def apply(index: Int)(using l: LayoutOf[A]) =
    Ptr[A](mem, offset + (l.layout.size * index))

  def castTo[A]: Ptr[A] = this.asInstanceOf[Ptr[A]]
  private[slinc] def resize(toBytes: Bytes) = Ptr[A](mem.resize(toBytes), offset)

object Ptr:
  extension (p: Ptr[Byte])
    def copyIntoString(maxSize: Int)(using LayoutOf[Byte]) =
      var i = 0
      val resizedPtr = p.resize(Bytes(maxSize))
      while (i < maxSize && !resizedPtr(i) != 0) do i += 1

      String(resizedPtr.asArray(i).unsafeArray, "ASCII")
  def blank[A](using layout: LayoutOf[A], alloc: Allocator): Ptr[A] =
    this.blankArray[A](1)

  def blankArray[A](
      num: Int
  )(using layout: LayoutOf[A], alloc: Allocator): Ptr[A] =
    Ptr[A](alloc.allocate(layout.layout, num), Bytes(0))

  def copy[A](
      a: Array[A]
  )(using alloc: Allocator, layout: LayoutOf[A], send: Send[Array[A]]) =
    val mem = alloc.allocate(layout.layout, a.size)
    send.to(mem, Bytes(0), a)
    Ptr[A](mem, Bytes(0))

  def copy[A](using alloc: Allocator)(
      a: A
  )(using send: Send[A], layout: LayoutOf[A]) =
    val mem = alloc.allocate(layout.layout, 1)
    send.to(mem, Bytes(0), a)
    Ptr[A](mem, Bytes(0))

  def copy(
      string: String
  )(using Allocator, LayoutOf[Byte], Send[Array[Byte]]): Ptr[Byte] = copy(
    string.getBytes("ASCII").nn :+ 0.toByte
  )

  inline def upcall[A](inline a: A)(using alloc: Allocator) =
    val nFn = Fn.toNativeCompatible(a)
    val descriptor = Descriptor.fromFunction[A]
    Ptr[A](alloc.upcall(descriptor, nFn), Bytes(0))
