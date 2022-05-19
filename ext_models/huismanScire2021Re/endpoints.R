source("estimate_Re.R")

#* @param data The estimated cases through time
#* @post /get-re
function(data) {
  data_cleaned <- tibble(data) %>%
    mutate(date = as.Date(date))
  result <- calculate(data_cleaned)
  return(result)
}
