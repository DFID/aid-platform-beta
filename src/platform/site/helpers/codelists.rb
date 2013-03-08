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

  def transaction_type(code)
    @@transaction_types[code]
  end

end