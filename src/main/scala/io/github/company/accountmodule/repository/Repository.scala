package io.github.company.accountmodule.repository

import scalaz.Monad
import scala.language.higherKinds

abstract class Repository[A, M[_]: Monad] {
	def +=(entity: A): M[A]
	def withdraw(entity: A): M[A]
	def deposit(entity: A): M[A]
	def move(id1: Int, id2: Int, amount: Int): M[A]
	def -=(entity: A): M[Unit]
	def list: M[List[A]]
}
