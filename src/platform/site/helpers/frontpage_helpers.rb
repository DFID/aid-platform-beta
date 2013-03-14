module FrontPageHelpers
  def what_we_do
    @cms_db['whatwedo'].find({})
  end

  def what_we_achieve
    @cms_db['whatweachieve'].find({})
  end
end