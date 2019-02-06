package de.dominicscheurer.fsautils {
  import Types._
import de.dominicscheurer.fsautils.FSA_DSL

  trait FSA_DSL {
    case class FSABuilder(
      t: (Symbol, Symbol, Symbol, Symbol, Symbol),
      isDFA: Boolean) {
      // The elements that need to be filled
      var alphabet: Option[Set[Letter]] = None
      var states: Option[States] = None
      var q0: Option[State] = None
      var deltaF: Option[(State, Letter) => State] = None
      var deltaR: Option[(State, Letter) => Set[State]] = None
      var A: Option[States] = None

      // Connectors for definition: "where" and "and"
      def where: ((Symbol, Any)) => FSA_DSL#FSABuilder =
        (input: (Symbol, Any)) =>
          (input._2 match {
            case symbols: SymbolSet =>
              input._1 match {
                case t._1 => { alphabet = Some(symbols.set); this }
              }
            case stateSet: IntSet =>
              input._1 match {
                case t._2 => {
                  states = Some(stateSet.set.map(s => q(s))); this
                }
                case t._5 => { A = Some(stateSet.set.map(s => q(s))); this }
              }
            case state: Int =>
              input._1 match {
                case t._3 => { q0 = Some(q(state)); this }
              }
            case func: DeltaFun =>
              input._1 match {
                case t._4 => {
                  def myDelta(s: State, l: Letter): State = s match {
                    case q(i) =>
                      if (func.fun contains (i, l))
                        q(func.fun(i, l))
                      else
                        throw new Exception(
                          "DFA transition function must be total, "
                            + "but is not defined for (" + i.toString + "," + l.toString + ")")
                    case _ => throw new Exception("Should not occur")
                  }
                  deltaF = Some(myDelta)
                  this
                }
              }
            case func: DeltaRel =>
              input._1 match {
                case t._4 => {
                  def myDelta(s: State, l: Letter): Set[State] = s match {
                    case q(i) =>
                      if (func.fun contains (i, l))
                        func.fun(i, l).map(s => q(s))
                      else
                        Set(): Set[State]
                    case _ => throw new Exception("Should not occur")
                  }
                  deltaR = Some(myDelta)
                  this
                }
              }
          })

      def and: ((Symbol, Any)) => FSA_DSL#FSABuilder = where

      def testDFADone: Boolean =
        alphabet.isDefined &&
          states.isDefined &&
          q0.isDefined &&
          deltaF.isDefined &&
          A.isDefined

      def testNFADone: Boolean =
        alphabet.isDefined &&
          states.isDefined &&
          q0.isDefined &&
          deltaR.isDefined &&
          A.isDefined

      def | : DFA =
        if (testDFADone)
          new DFA(alphabet.get, states.get, q0.get, deltaF.get, A.get)
        else
          throw new Exception("Some values of the DFA are still undefined")

      def || : NFA =
        if (testNFADone)
          new NFA(alphabet.get, states.get, q0.get, deltaR.get, A.get)
        else
          throw new Exception("Some values of the NFA are still undefined")
    }

    // Starting point: dfa/nfa function
    def dfa(t: (Symbol, Symbol, Symbol, Symbol, Symbol)): FSA_DSL#FSABuilder =
      FSABuilder(t, true)
    def nfa(t: (Symbol, Symbol, Symbol, Symbol, Symbol)): FSA_DSL#FSABuilder =
      FSABuilder(t, false)

    // Syntactic Sugar
    object Delta {
      def apply(t: ((Int, Symbol), Int)*): FSA_DSL#DeltaFun = DeltaFun(Map() ++ t)
      def apply(t: ((Int, Symbol), Set[Int])*): FSA_DSL#DeltaRel = DeltaRel(Map() ++ t)
    }

    case class SymbolWrapper(s: Symbol) {
      def ==>(vals: SymbolSet): (Symbol, FSA_DSL#SymbolSet) = (s, vals)
      def ==>(vals: IntSet): (Symbol, FSA_DSL#IntSet) = (s, vals)
      def ==>(aval: Int): (Symbol, Int) = (s, aval)
      def ==>(afun: DeltaFun): (Symbol, FSA_DSL#DeltaFun) = (s, afun)
      def ==>(afun: DeltaRel): (Symbol, FSA_DSL#DeltaRel) = (s, afun)
    }

    implicit def SymbToSymbWrapper(s: Symbol): FSA_DSL#SymbolWrapper = SymbolWrapper(s)

    case class IntSet(set: Set[Int])
    case class StringSet(set: Set[String])
    case class SymbolSet(set: Set[Symbol])
    case class DeltaFun(fun: Map[(Int, Letter), Int])
    case class DeltaRel(fun: Map[(Int, Letter), Set[Int]])
    implicit def is(set: Set[Int]): FSA_DSL#IntSet = IntSet(set)
    implicit def sts(set: Set[String]): FSA_DSL#StringSet = StringSet(set)
    implicit def sys(set: Set[Symbol]): FSA_DSL#SymbolSet = SymbolSet(set)
    //		implicit def dfun(fun: Map[(Int, Letter), Int]) = DeltaFun(fun)
  }
}
