package ru.neoflex.imdbApp.kryo

import com.esotericsoftware.kryo.io.{ Input, Output }

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.FieldSerializer

class MyObject() {
  var _message: String = ""
  var _number:  Int    = 0

  def this(
    message: String,
    number:  Int
  ) = {
    this()
    _message = message
    _number = number
  }

  override def toString(): String = s"MyObject[${_message}, ${_number}]"
}

/** пример работы с kryo (не Spark)
  */
object KryoExample1 extends App {

// Определение класса

  val kryo = new Kryo()

//   // Регистрация класса для сериализации (опционально, но рекомендуется для повышения производительности)
//   kryo.register(classOf[MyObject])

  // Использование FieldSerializer
  kryo.register(classOf[MyObject])
  kryo.register(classOf[List[String]])

  // Создание объекта
  val myObject = new MyObject("Hello Kryo", 123)

  // Сериализация объекта в байтовый массив
  val outputStream = new ByteArrayOutputStream()
  val output       = new Output(outputStream)
  kryo.writeObject(output, myObject)
  output.close()

  val serializedBytes = outputStream.toByteArray

  // Десериализация объекта из байтового массива
  val inputStream = new ByteArrayInputStream(serializedBytes)
  val input       = new Input(inputStream)
  val deserializedObject: MyObject = kryo.readObject(input, classOf[MyObject])
  input.close()

  // Печать десериализованного объекта
  println(deserializedObject)

  //list
//   val baos = new ByteArrayOutputStream()
//   val out  = new Output(baos)
//   kryo.writeObject(
//     out,
//     List[String]("A", "B", "C")
//   )
//   out.close()

//   val bytes = baos.toByteArray

//   // Десериализация объекта из байтового массива
//   val bais = new ByteArrayInputStream(serializedBytes)
//   val in   = new Input(bais)
//   val list = kryo.readObject(in, classOf[List[String]])
//   in.close()

//   println(list.mkString(","))

}
