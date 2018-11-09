package io.github.company.accountmodule.service

import scalaz._, Scalaz._
import io.circe._
import org.http4s._
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.dsl._
import scalaz.concurrent.Task

import io.github.company.accountmodule.domain._
import io.github.company.accountmodule.repository.Repository
import io.github.company.accountmodule.repository.impl.H2AccountRepository

object AccountService {
	implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[A] = org.http4s.circe.jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[A] = org.http4s.circe.jsonEncoderOf[A]

	val repository: Task[Repository[Account, Task]] = H2AccountRepository()

	val service = HttpService {
		case GET -> Root => repository >>= (_.list) >>= (Ok(_))
		case req @ POST -> Root => 	req.decode[Account]{ data =>
			Ok(repository >>= (_ += data))
		}
		case req @ PUT -> Root => req.decode[Account]{ data =>
			Ok(repository >>= (_ update data))
		}
		case req @ PUT -> Root / "withdraw"  => req.decode[Account]{ data =>
			Ok(repository >>= (_ withdraw data))
		}.handleErrorWith {
			case InvalidAmount(amount) => BadRequest(s"You dont't have enough currency to withdraw $amount")
		}
		case req @ PUT -> Root / "deposit"  => req.decode[Account]{ data =>
			Ok(repository >>= (_ deposit data))
		}
		case req @ PUT -> Root / "move"  => req.decode[MoveRequest]{ data =>
			Ok(repository >>= (_ move (data.id1, data.id2, data.amount)))
		}.handleErrorWith {
			case InvalidAmount(amount) => BadRequest(s"You dont't have enough currency to withdraw $amount")
		}
		case DELETE -> Root / id => req.decode[Int]{ id => Ok(repository >>= (_ -= Account(id)))}
	}
}
