package ru.neoflex.imdbApp.kryo

import org.apache.spark.serializer.KryoRegistrator
import com.esotericsoftware.kryo.Kryo
import scala.collection.mutable.ListBuffer

class KryoReg extends KryoRegistrator {

  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[ListBuffer[String]])
    kryo.register(classOf[java.util.ArrayList[String]])
    // kryo.register(classOf[List[String]])
    // kryo.register(classOf[Array[String]])
  }
}
