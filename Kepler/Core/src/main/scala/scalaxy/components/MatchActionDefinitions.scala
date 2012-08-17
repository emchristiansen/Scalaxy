package scalaxy; package components

//import scala.reflect.mirror._
import scala.reflect.runtime._

object MatchActionDefinitions 
{
  
  private def defaultValue(c: Class[_]): AnyRef = c match {
    case _ if c == classOf[Int] => java.lang.Integer.valueOf(0)
    case _ if c == classOf[Long] => java.lang.Long.valueOf(0L)
    case _ if c == classOf[Short] => java.lang.Short.valueOf(0: Short)
    case _ if c == classOf[Char] => java.lang.Character.valueOf('\0')
    case _ if c == classOf[Boolean] => java.lang.Boolean.FALSE
    case _ if c == classOf[Double] => java.lang.Double.valueOf(0.0)
    case _ if c == classOf[Float] => java.lang.Float.valueOf(0.0f)
    case _ if c == classOf[Byte] => java.lang.Byte.valueOf(0: Byte)
    //case _ if classOf[TypeTag[_]].isAssignableFrom(c) => scala.reflect.mirror.ConcreteTypeTag.Any
    case _ => null
  }
  
  def getMatchActionDefinitions(holder: AnyRef): Seq[(String, MatchAction)] = {
    var nTypeTags = 0
    for (m <- holder.getClass.getMethods; 
         if classOf[MatchAction].isAssignableFrom(m.getReturnType)
    ) yield 
    {
      val r = m.invoke(holder, m.getParameterTypes.map(c => {
        if (c == classOf[universe.TypeTag[_]]) {
          val tt = TypeVars.getNthTypeVarTag(nTypeTags)
          nTypeTags += 1
          tt
        } else {
          defaultValue(c)
        }
      }):_*)
      
      (m.getName, r.asInstanceOf[MatchAction]) 
    }
  }
  /**
   * Scala reflection is just way too broken as of 2.10.0-M2 / M3 (with objects, at least)
   * TODO try again calling compilet methods using Scala reflection in later versions !
   */
  /*
  import scala.reflect.mirror._
  import definitions._
  
  private lazy val MatchActionClass = staticClass("scalaxy.MatchAction")
  
  private lazy val defaultValues = Map(
    IntTpe -> 0,
    ShortTpe -> (0: Short),
    LongTpe -> (0: Long),
    ByteTpe -> (0: Byte),
    FloatTpe -> 0f,
    DoubleTpe -> 0.0,
    BooleanTpe -> false,
    CharTpe -> '\0'
  )
  private def getDefaultValue(tpe: Type): Any = 
    defaultValues.get(tpe).getOrElse(null)
  
  def invokeMethod(target: AnyRef, method: Symbol, params: List[AnyRef]) = {
    //println("target = " + target)
    if (true) {
      val methodName = method.name.toString
      val javaMethod = target.getClass.getDeclaredMethods.find(_.getName == methodName).getOrElse(
        throw new RuntimeException("No method '" + methodName + "' found in " + target.getClass.getName + ":\n" + getClass.getDeclaredMethods.mkString("\n"))
      )
      javaMethod.invoke(target, params:_*)
    } else {
      invoke(target, method)(params)
    }
  }
  
  @deprecated("Broken code, try again with 2.10.0 final !")
  private def getMatchActionDefinitionsWithReflection(holder: AnyRef): Seq[(String, MatchAction)] = {
    //val holder = Example
    //val holderSym = staticClass("scalaxy.Example")
    //val holder = companionInstance(holderSym)
    val holderType = 
      //classToType(holder.getClass)
      typeOfInstance(holder)
      
    //val holderCls = classOf[Example]
    //val holderSym = classToSymbol(holderCls)//staticClass("scalaxy.Example")
    //val holderType = classToType(holderCls)//holderSym.tpe//.deconst.dealias
    //val holder = holderCls.newInstance.asInstanceOf[AnyRef]
    
    def dbgStr(t: Type) = {
      t + " (" + t.getClass.getName + " <- " + t.getClass.getSuperclass.getName + ") = " + t//debugString(t)
    }
    object FollowMethodResult {
      def unapply(t: Type): Option[Type] = t match {
        case MethodType(_, r) =>
          unapply(r)
        case PolyType(_, mt) =>
          unapply(mt)
        case _ =>
          Some(t)
      }
    }
    val methods: Seq[(Symbol, Type)] = 
      holderType.members.filter(_.isMethod).map(m => (m, m.asType)).toSeq
    //println("Scanning holder " + holder + " : " + methods.size + " methods")
    //for (m <- methods) println("\t" + m.name)
      
    methods.collect({ 
      //case (m, PolyType(paramsyms, mt @ FollowMethodResult(result)))
      case (m, mt @ FollowMethodResult(result))
      =>
      // TODO
      //if result.stat_<:<(MatchActionClass.tpe) => // == ReplacementClass.tpe =>
        def getParamTypes(t: Type): List[Type] = t match {
          case MethodType(tt, r) =>
            //println("Found method (" + m.name + ", m.type = " + dbgStr(m.tpe) + ") : type " + dbgStr(t) + " with tt = " + tt + " and r = " + r)
            tt.map(_.asType) ++ getParamTypes(r)
          case PolyType(_, mt) =>
            getParamTypes(mt)
          case _ =>
            List()
        }
        val actualParamTypes = getParamTypes(mt)
        val defaultParams = 
          actualParamTypes.map(getDefaultValue(_).asInstanceOf[AnyRef])
          
        try {
          val r = invokeMethod(holder, m, defaultParams)
          //println("r = " + r + " : " + r.getClass.getName)
          if (r.isInstanceOf[MatchAction])
            Some((holder + "." + m.name, r.asInstanceOf[MatchAction]))
          else 
            None
        } catch { case ex =>
          None
        }
      case (m, t) =>
        println("Unable to parse method '" + m.name + "' : " + dbgStr(t))
        None
    }).flatten
  }
  */
}
