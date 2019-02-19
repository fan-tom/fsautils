package de.dominicscheurer.fsautils

import scala.language.postfixOps
import de.dominicscheurer.fsautils.Types.Letter

object RegularExpressions {

  sealed abstract class RE extends FSA_DSL {
    type MutableMap[A, B] = scala.collection.mutable.Map[A, B]
    type Map[A, B] = scala.collection.immutable.Map[A, B]
    def MutableMap[A, B](): MutableMap[A, B] = collection.mutable.Map[A, B]()

    def *(): RE = Star(this)
    def +(rhs: RE): RE = Or(this, rhs)
    def &(rhs: RE): RE = Concat(this, rhs)

    def alphabet: Set[Letter]

    def toNFA: NFA = toNFAInt(alphabet, MutableMap())
    def toNFAInt(alph: Set[Letter], cache: MutableMap[RE, NFA]): NFA

    override def hashCode: Int = toString hashCode
    override def equals(other: Any): Boolean =
      other.isInstanceOf[RE] && (this.hashCode == other.hashCode)

    /**
     * cleanString does some post processing on the
     * toString method in order to make the output better
     * readable. However, you naturally achieve better
     * correctness guarantees without this method (since
     * this is just string manipulation with regular expressions).
     */
    def cleanString: String = recClean(toString)

    private def recClean(s: String): String = {
      val cleanRes = clean(s)
      if (s equals cleanRes) {
        s
      } else {
        recClean(cleanRes)
      }
    }

    private def clean(s: String): String =
      s.replace("{} + ", "")
        .replace("({})*", "\u025B") // epsilon
        .replace("{}", "\u00D8") // emptyset
        .replace("**", "*")
        .replaceAll("""'([a-z])""", "$1")
        .replaceAll("""\(([a-z])\)""", "$1")
        .replaceAll("""\(\(([^\(\)]+)\)\)\*""", "($1)*")
        .replaceAll("""\(\u025B \+ ([^\(\)]+)\)\*""", "($1)*")
        .replaceAll(""" [&\+] \u00D8""", "")
        .replaceAll("""\u00D8 [&\+] """, "")
        .replaceAll("""\(([a-z\u025B])\)([\*]?)""", "$1$2")
        .replaceAll("""\(\(([^\(\)]+)\)\)""", "($1)")
        .replaceAll("""\(([a-z])\*\)""", "$1*")

    def clean: RE = this match {
      case Star(inner) =>
        inner match {
          // ((...)*)* => (...)*
          case Star(inner2) => Star(inner2 clean)
          // ({}* + XXX)* => (XXX)*
          case Or(Star(Empty()), rhs) => rhs clean
          // (XXX + {}*)* => (XXX)*
          case Or(lhs, Star(Empty())) => lhs clean
          case _ => Star(inner clean)
        }
      case Or(lhs, rhs) =>
        lhs match {
          // {} + (...) => (...)
          case Empty() => rhs clean
          case Star(Empty()) =>
            rhs match {
              // {}* + (...)* => (...)*
              case Star(rhsInner) => Star(rhsInner clean)
              case _ => Or(lhs clean, rhs clean)
            }
          case _ =>
            rhs match {
              // (...) + {} => (...)
              case Empty() => lhs clean
              case Star(Empty()) =>
                lhs match {
                  // (...)* + {}* => (...)*
                  case Star(lhsInner) => Star(lhsInner clean)
                  case _ => Or(lhs clean, rhs clean)
                }
              case _ =>
                if (lhs equals rhs)
                  // XXX + XXX => XXX
                  lhs
                else
                  Or(lhs clean, rhs clean)
            }
        }
      case Concat(lhs, rhs) =>
        lhs match {
          // {} & (...) => (...)
          case Empty() => rhs clean
          case Or(Star(Empty()), lhsInner) =>
            rhs match {
              case Star(rhsInner) => {
                val lhsInnerClean = lhsInner clean
                val rhsInnerClean = rhsInner clean

                if (lhsInnerClean equals rhsInnerClean)
                  // (eps + XXX) & (XXX)* => (XXX)*
                  Star(rhsInnerClean)
                else
                  Concat(lhs clean, rhs clean)
              }
              case _ => Concat(lhs clean, rhs clean)
            }
          case Or(lhsInner, Star(Empty())) =>
            rhs match {
              case Star(rhsInner) => {
                val lhsInnerClean = lhsInner clean
                val rhsInnerClean = rhsInner clean

                if (lhsInnerClean equals rhsInnerClean)
                  // (XXX + eps) & (XXX)* => (XXX)*
                  Star(rhsInnerClean)
                else
                  Concat(lhs clean, rhs clean)
              }
              case _ => Concat(lhs clean, rhs clean)
            }
          case _ =>
            rhs match {
              // (...) + {} => (...)
              case Empty() => lhs clean
              case Or(rhsInner, Star(Empty())) =>
                lhs match {
                  case Star(lhsInner) =>
                    val lhsInnerClean = lhsInner clean
                    val rhsInnerClean = rhsInner clean

                    if (lhsInnerClean equals rhsInnerClean)
                      // (XXX)* & (XXX + eps) => (XXX)*
                      Star(lhsInnerClean)
                    else
                      Concat(lhs clean, rhs clean)

                  case _ => Concat(lhs clean, rhs clean)
                }
              case Or(Star(Empty()), rhsInner) =>
                lhs match {
                  case Star(lhsInner) =>
                    val lhsInnerClean = lhsInner clean
                    val rhsInnerClean = rhsInner clean

                    if (lhsInnerClean equals rhsInnerClean)
                      // (XXX)* & (eps + XXX) => (XXX)*
                      Star(lhsInnerClean)
                    else
                      Concat(lhs clean, rhs clean)

                  case _ => Concat(lhs clean, rhs clean)
                }
              case _ => Concat(lhs clean, rhs clean)
            }
        }
      case _ => this
    }
  }

  case class L(l: Letter) extends RE {
    override def toString: String = l toString
    override def alphabet: Set[Letter] = Set(l)
    override def toNFAInt(
      alph: Set[Letter],
      cache: MutableMap[RE, NFA]): NFA = {
      val genNFA = nfa(t = ('Z, 'S, 'q0, 'd, 'A)) where
        'Z ==> alph and
        'S ==> Set(0, 1) and
        'q0 ==> 0 and
        'A ==> Set(1) and
        'd ==> Delta((0, l) -> Set(1)) ||

      cache += (this -> genNFA)
      genNFA
    }
  }

  case class Empty() extends RE {
    override def toString: String = "{}"
    override def alphabet: Set[Letter] = Set()
    override def toNFAInt(
      alph: Set[Letter],
      cache: MutableMap[RE, NFA]): NFA = {
      val emptyAcc: Set[Int] = Set()
      val genNFA = nfa(t = ('Z, 'S, 'q0, 'd, 'A)) where
        'Z ==> alph and
        'S ==> Set(0) and
        'q0 ==> 0 and
        'A ==> emptyAcc and
        'd ==> DeltaRel(Map()) ||

      cache += (this -> genNFA)
      genNFA
    }
  }

  case class Star(re: RE) extends RE {
    override def toString: String = "(" + re.toString + ")*"
    override def alphabet: Set[Letter] = re.alphabet
    override def toNFAInt(alph: Set[Letter], cache: MutableMap[RE, NFA]): NFA =
      if (re equals Empty())
        nfa(t = ('Z, 'S, 'q0, 'd, 'A)) where
          'Z ==> alph and
          'S ==> Set(0) and
          'q0 ==> 0 and
          'A ==> Set(0) and
          'd ==> DeltaRel(Map()) ||
      else {
        cache get this match {
          case None =>
            val genNFA = (re toNFAInt (alph, cache)) *

            cache += (this -> genNFA)
            genNFA

          case Some(nfa) => nfa
        }
      }
  }

  case class Or(lhs: RE, rhs: RE) extends RE {
    override def toString: String =
      "(" + lhs.toString + " + " + rhs.toString + ")"
    override def alphabet: Set[Letter] = lhs.alphabet ++ rhs.alphabet
    override def toNFAInt(alph: Set[Letter], cache: MutableMap[RE, NFA]): NFA =
      cache get this match {
        case None => {
          val genNFA =
            (lhs toNFAInt (alph, cache)) | (rhs toNFAInt (alph, cache)): NFA

          cache += (this -> genNFA)
          genNFA
        }
        case Some(nfa) => nfa
      }
  }

  case class Concat(lhs: RE, rhs: RE) extends RE {
    override def toString: String =
      "(" + lhs.toString + " & " + rhs.toString + ")"
    override def alphabet: Set[Letter] = lhs.alphabet ++ rhs.alphabet
    override def toNFAInt(alph: Set[Letter], cache: MutableMap[RE, NFA]): NFA =
      cache get this match {
        case None => {
          val genNFA =
            (lhs toNFAInt (alph, cache)) ++ (rhs toNFAInt (alph, cache)): NFA

          cache += (this -> genNFA)
          genNFA
        }
        case Some(nfa) => nfa
      }
  }
}
