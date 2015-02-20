package uk.gov.dfid.loader.util

object IatiCodeListConverter
{

   def transactionType(input: String) {

        input match {
            case "1" => "IF"
            case "2" => "C"
            case "3" => "D"
            case "4" => "E"
            case "5" => "IR"
            case "6" => "LR"
            case "7" => "R"
            case "8" => "QP"
            case "9" => "Q3"
            case "10" => "CG"
            case _ => ""
        }
    }



}
