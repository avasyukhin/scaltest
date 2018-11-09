package io.github.company.accountmodule.domain

sealed trait AccountError extends Exception
case class AccountAlreadyExists(id: Int) extends AccountError
case class AccountNotFound(id: Int) extends AccountError
case class InvalidAmount(amount: Int) extends AccountError
