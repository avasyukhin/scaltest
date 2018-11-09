package io.github.company.accountmodule.repository.impl

import scalaz._, Scalaz._
import doobie.imports._
import doobie.h2.imports._
import scalaz.concurrent.Task

import io.github.company.accountmodule.domain._
import io.github.company.accountmodule.repository.Repository

object H2AccountRepository {
	def apply() = Task {
		val h2nr = new H2AccountRepository()
		h2nr.init.unsafePerformSync
		h2nr
	}
}

class H2AccountRepository extends Repository[Account, Task]{

	val xa = H2Transactor[Task]("jdbc:h2:mem:accountmodule;DB_CLOSE_DELAY=-1", "h2username", "h2password")

	private def init = {
		val query = sql"""
	     CREATE TABLE IF NOT EXISTS account (
	       id INT NOT NULL UNIQUE,
				 amount  INT
	     )
	    """.update.run
		xa >>= (query.transact(_))
	}

	override def +=(account: Account) = {
		val insertQ = sql"""
				INSERT INTO account (id, amount)
				VALUES (${account.id},${account.amount})
			""".update.run
    val selectQ = sql"""
				SELECT id, amount
				FROM account
        WHERE id = ${account.id}
  		""".query[Account].unique
		val query = insertQ *> selectQ
		xa >>= (query.transact(_))
	}


	override def withdraw(account: Account) = {
		val selectQ = sql"""
				SELECT amount
				FROM account
		WHERE id = ${account.id}
		""".query[Int]
		val storedAmount = xa >>= (selectQ.transact(_))
		if (storedAmount < account.amount) Task.raiseError(InvalidAmount(account.amount))
		val newAmount = storedAmount - account.amount
		val updateQ = sql"""
				UPDATE account
				SET amount = ${newAmount}
		WHERE id = ${account.id} 
		"""	
		val selectResult = sql"""
				SELECT id, amount
				FROM account
        WHERE id = ${account.id}
  		""".query[Account].unique
		val query = updateQ *> selectResult
		xa >>= (query.transact(_))
	}

	override def deposit(account: Account)={
		val selectQ = sql"""
				SELECT amount
				FROM account
		WHERE id = ${account.id}
		""".query[Int]
		val storedAmount = xa >>= (selectQ.transact(_))
		val newAmount = storedAmount + account.amount
		val updateQ = sql"""
				UPDATE account
				SET amount = ${newAmount}
		WHERE id = ${account.id} 
		"""	
		val selectResult = sql"""
				SELECT id, amount
				FROM account
        WHERE id = ${account.id}
  		""".query[Account].unique
		val query = updateQ *> selectResult
		xa >>= (query.transact(_))
	}


	override def move(sourceId: Int, recievingId: Int, amount: Int) = {
		val selectQSource = sql"""
				SELECT amount
				FROM account
		WHERE id = ${sourceId}
		""".query[Int]
		val sourceAmount = xa >>= (selectQSource.transact(_))
		if (sourceAmount < amount) Task.raiseError(InvalidAmount(amount))
		val selectQRecieve = sql"""
				SELECT amount
				FROM account
		WHERE id = ${recievingId}
		""".query[Int]
		val recievingAmount = xa >>= (selectQ.transact(_))
		val newStoreAmount = sourceAmount - amount
		val newReciveAmount = amount + recievingAmount
		val updateStoreQ = sql"""
				UPDATE account
				SET amount = ${newStoreAmount}
		WHERE id = ${sourceId} 
		"""	
		val updateReciveQ = sql"""
				UPDATE account
				SET amount = ${newReciveAmount}
		WHERE id = ${recievingId} 
		"""	
		val selectQ = sql"""
				SELECT id, amount
				FROM account
        WHERE id = ${sourceId}
  		""".query[Account].unique
		val query = updateStoreQ *> updateReciveQ *> selectQ
		xa >>= (query.transact(_))
	}

	override def -=(account: Account) = {
		val query = sql"""
				DELETE FROM account
				WHERE id = ${account.id}
			""".update.run *> FC.unit
		xa >>= (query.transact(_))
	}

	override def list = {
		val query = sql"""
				SELECT id, amount
				FROM account
			""".query[Account].list
		xa >>= (query.transact(_))
	}
}
