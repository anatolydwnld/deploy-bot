package me.dwnld.utils

import java.time.Duration

case class RepoState(
  lag: Duration,
  aheadBy: Int,
  behindBy: Int
)
