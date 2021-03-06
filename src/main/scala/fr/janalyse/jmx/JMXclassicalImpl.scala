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

import javax.management.remote.JMXConnector
import javax.management.ObjectName
import javax.management.MBeanServerConnection
import scala.collection.JavaConverters._
import javax.management.MBeanInfo
import javax.management.RuntimeMBeanException
import java.rmi.UnmarshalException

private class JMXclassicalImpl(
  conn: JMXConnector,
  val options: Option[JMXOptions] = None,
  additionalCleaning: Option[() => Any] = None) extends JMXJsr160 {
  private lazy val mbsc: MBeanServerConnection = conn.getMBeanServerConnection

  def close() = {
    try {
      conn.close()
    } catch {
      case e: Exception => logger.warn("Exception while closing rmi connection, let's ignore and continue..."+e.getMessage)
    }
    try {
      additionalCleaning.foreach{_()}
    } catch {
      case e: Exception => logger.warn("Exception in additionnal cleaning procesure, let's ignore and continue..."+e.getMessage)
    }
  }

  def getMBeanInfo(objectName: ObjectName):MBeanInfo = mbsc.getMBeanInfo(objectName)
  
  def getAttribute(objectName: ObjectName, attrname: String):Option[Object] = {
    try {
	    val res = mbsc.getAttribute(objectName, attrname)
	    //Option(convert(res))
	    Option(res)
    } catch {
      case e: RuntimeMBeanException /* if e.getCause().isInstanceOf[UnsupportedOperationException] */=> None
      case e: javax.management.MBeanException => None
      case e: javax.management.RuntimeOperationsException => None
      case e: javax.management.ReflectionException => None
      case e: javax.management.AttributeNotFoundException => None
      case e: UnmarshalException => None
      case e: java.lang.IllegalArgumentException => None
      case e: java.rmi.ConnectException => throw e
      case e: java.net.ConnectException => throw e
      case e: java.net.SocketException => throw e
      case e: javax.management.InstanceNotFoundException => throw e
      case e: java.io.IOException => throw e //None
      case x: Exception =>
        logger.warn("Warning: Error while getting value for attribute "+attrname+" mbean "+objectName.getCanonicalName(), x)
        None
    }
  }
  
  def setAttribute(objectName: ObjectName, attrname: String, attrvalue: Any) {
    val attribute = new javax.management.Attribute(attrname, attrvalue)
    mbsc.setAttribute(objectName, attribute)
  }
  def invoke(objectName: ObjectName, operationName: String, args: Array[Any]): Option[Any] = {
    Option(mbsc.invoke(objectName, operationName, args.map(_.asInstanceOf[Object]), buildOperationSignature(args)))
  }
  private def newMBean(objectName: ObjectName) =
    RichMBean(
      objectName,
      () => getAttributesMap(objectName),
      (attrname) => getAttribute(objectName, attrname),
      (attrname, attrval) => setAttribute(objectName, attrname, attrval),
      (operationName, args) => invoke(objectName, operationName, args)
    )
  def domains: List[String] = mbsc.getDomains().toList
  def names(query:String):List[String]=mbsc.queryNames(null, string2objectName(query)).asScala.toList.map(_.getCanonicalName())
  override def names():List[String]=mbsc.queryNames(null, null).asScala.toList.map(_.getCanonicalName())
  def exists(name: String): Boolean = mbsc.queryNames(name, null).size > 0
  def apply(name: String): RichMBean = newMBean(name)
  def mbeans(query: String): List[RichMBean] = mbsc.queryNames(null, string2objectName(query)).asScala.toList map { newMBean(_) }
  override def mbeans(): List[RichMBean] = mbsc.queryNames(null, null).asScala.toList map { newMBean(_) }
}

