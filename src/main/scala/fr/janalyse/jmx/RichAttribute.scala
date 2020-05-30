/*
 * Copyright 2013 David Crosson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.janalyse.jmx

import javax.management.openmbean.{ CompositeData, CompositeDataSupport }
//import org.json4s._


sealed trait RichAttribute {
  val name: String
  val desc: Option[String]
  def asString(ob: Object): String = ob match {
  //  case JString(str) => str
    case x => x.toString() // TODO BAD
  }
}

sealed trait RichArrayAttribute extends RichAttribute {
}

sealed trait RichNumberAttribute extends RichAttribute {
  def asDouble(ob: Object): Double = ob match { // TODO BAD
    case e: java.lang.Byte => e.toDouble
    case e: java.lang.Short => e.toDouble
    case e: java.lang.Integer => e.toDouble
    case e: java.lang.Long => e.toDouble
    case e: java.lang.Float => e.toDouble
    case e: java.lang.Double => e
    case e: java.lang.String => e.toDouble
    case e: BigInt => e.toDouble
    case e: BigDecimal => e.toDouble
  }
  def asLong(ob: Object): Long = ob match { // TODO BAD
    case e: java.lang.Byte => e.toLong
    case e: java.lang.Short => e.toLong
    case e: java.lang.Integer => e.toLong
    case e: java.lang.Long => e
    case e: java.lang.Float => e.toLong
    case e: java.lang.Double => e.toLong
    case e: java.lang.String => e.toLong
    case e: BigInt => e.toLong
    case e: BigDecimal => e.toLong
  }
  def asInt(ob: Object): Int = ob match { // TODO BAD
    case e: java.lang.Byte => e.toInt
    case e: java.lang.Short => e.toInt
    case e: java.lang.Integer => e
    case e: java.lang.Long => e.toInt
    case e: java.lang.Float => e.toInt
    case e: java.lang.Double => e.toInt
    case e: java.lang.String => e.toInt
    case e: BigInt => e.toInt
    case e: BigDecimal => e.toInt
  }

}

final case class RichCompositeDataAttribute(name: String, desc: Option[String] = None) extends RichAttribute {
  override def asString(ob: Object): String = {
    val cd = ob.asInstanceOf[CompositeData] // TODO BAD
    cd.toString()
  }
  def asMap(ob: Object) = ob.asInstanceOf[CompositeData].content // TODO BAD
  def asNumberMap[N >: Number](ob: Object): Map[String, N] = {
    val cd = ob.asInstanceOf[CompositeData] // TODO BAD
    cd.content flatMap {
      case (name, value) =>
        value match {
          case x: java.lang.Long    => Some((name, x))//Some(name -> new runtime.RichDouble(x.toDouble))
          case x: java.lang.Float   => Some((name, x))//Some(name -> new runtime.RichDouble(x.toDouble))
          case x: java.lang.Double  => Some((name, x))//Some(name -> new runtime.RichDouble(x.toDouble))
          case x: java.lang.Integer => Some((name, x))//Some(name -> new runtime.RichDouble(x.toDouble))
          case _ => None
        }
    }
  }
}

final case class RichStringArrayAttribute(name: String, desc: Option[String] = None) extends RichArrayAttribute {
  override def asString(ob: Object): String = { // TODO BAD
    ob.asInstanceOf[Array[String]].toList.mkString(", ")
  }
}

final case class RichBooleanAttribute(name: String, desc: Option[String] = None) extends RichAttribute

final case class RichStringAttribute(name: String, desc: Option[String] = None) extends RichAttribute

final case class RichGenericAttribute(name: String, desc: Option[String] = None) extends RichAttribute

final case class RichByteAttribute(name: String, desc: Option[String] = None) extends RichNumberAttribute
final case class RichShortAttribute(name: String, desc: Option[String] = None) extends RichNumberAttribute
final case class RichIntAttribute(name: String, desc: Option[String] = None) extends RichNumberAttribute
final case class RichLongAttribute(name: String, desc: Option[String] = None) extends RichNumberAttribute
final case class RichDoubleAttribute(name: String, desc: Option[String] = None) extends RichNumberAttribute
final case class RichFloatAttribute(name: String, desc: Option[String] = None) extends RichNumberAttribute
