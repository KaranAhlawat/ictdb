package io.karan.ictdb.modules

import io.karan.ictdb.persistence.user.{UserRepository, UserRepositoryLive}

case class AppRepositories(userRepo: UserRepository)

object AppRepositories:
    def make(resources: AppResources): AppRepositories =
        AppRepositories(UserRepositoryLive.make(resources.dbPool))
