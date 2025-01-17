package fr.hammons.slinc

import java.lang.invoke.MethodHandle
import jdk.incubator.foreign.FunctionDescriptor
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.MemoryAddress
import java.lang.invoke.MethodType
import jdk.incubator.foreign.CLinker
import jdk.incubator.foreign.SymbolLookup
import jdk.incubator.foreign.Addressable
import java.nio.file.Paths
import java.nio.file.Files

class Library17(layoutI: LayoutI, linker: CLinker)
    extends LibraryI.PlatformSpecific(layoutI):
  import layoutI.*

  override def getDowncall(
      address: Object,
      descriptor: Descriptor
  ): MethodHandle =
    val fd = descriptor.outputLayout match
      case Some(r) =>
        FunctionDescriptor.of(
          LayoutI17.dataLayout2MemoryLayout(r),
          descriptor.inputLayouts
            .map(LayoutI17.dataLayout2MemoryLayout)
            .concat(
              descriptor.variadicLayouts
                .map(LayoutI17.dataLayout2MemoryLayout)
                .map(CLinker.asVarArg)
            )*
        )
      case _ =>
        FunctionDescriptor.ofVoid(
          descriptor.inputLayouts
            .map(LayoutI17.dataLayout2MemoryLayout)
            .concat(
              descriptor.variadicLayouts
                .map(LayoutI17.dataLayout2MemoryLayout)
                .map(CLinker.asVarArg)
            )*
        )

    val md = descriptor.toMethodType

    linker.downcallHandle(address.asInstanceOf[Addressable], md, fd).nn

  override def getLookup(name: Option[String]): Lookup =
    import scala.jdk.OptionConverters.*
    name match
      case Some(n) =>
        new Lookup:
          if Files.exists(Paths.get(n)) then
            System.load(Paths.get(n).nn.toRealPath().nn.toString())
          else System.loadLibrary(n)
          val l = SymbolLookup.loaderLookup().nn
          def lookup(name: String) = l
            .lookup(name)
            .nn
            .toScala
            .getOrElse(throw Error(s"Failed to load $name from $n"))
      case None =>
        new Lookup:
          val l = CLinker.systemLookup().nn
          def lookup(name: String) = l
            .lookup(name)
            .nn
            .toScala
            .getOrElse(
              throw Error(s"Failed to load $name from standard library")
            )
