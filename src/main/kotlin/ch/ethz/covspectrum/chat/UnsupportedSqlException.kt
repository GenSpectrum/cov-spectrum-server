package ch.ethz.covspectrum.chat

class UnsupportedSqlException(sql: String): Exception("The generated SQL is not supported: $sql")
