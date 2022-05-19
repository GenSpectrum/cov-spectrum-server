library(plumber)

pr("endpoints.R") %>%
  pr_run(
    host = "0.0.0.0",
    port = 7080
  )
