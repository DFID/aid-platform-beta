module CodeLists

  @@transaction_types = {
    "C"  => "Commitment",            
    "D"  => "Disbursement",          
    "E"  => "Expenditure",         
    "IF" => "Incoming Funds",      
    "IR" => "Interest Repayment",  
    "LR" => "Loan Repayment",      
    "R " => "Reimbursement",         
    "QP" => "Purchase of Equity",  
    "QS" => "Sale of Equity",      
    "CG" => "Credit Guarantee"  
  }

  @@activity_statuses = {
    1 => "Pipeline/identification",
    2 => "Implementation",
    3 => "Completion",
    4 => "Post-completion",
    5 => "Cancelled"
  }

  def transaction_type(code)
    @@transaction_types[code]
  end

  def activity_status(code)
    @@activity_statuses[code]
  end

end