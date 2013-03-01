module Lookups

  def activty_status(code)
    {
      1 => "Pipeline/identification",
      2 => "Implementation",
      3 => "Completion",
      4 => "Post-completion",
      5 => "Cancelled"
    }[code]
  end
end