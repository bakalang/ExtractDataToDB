# ExtractDataToDB 

extract data from json format url response, or files

/** data from files **/
<csvData>
  <uploadFolder>D:\bak\db</uploadFolder>
  <extractThread>3</extractThread>
  <skipLine>1</skipLine>
  
  <column>project_no</column>
  <column>depend_on</column>
</csvData>

/** data from json **/
<jsonData>
  <sourceURL>http://data.fda.gov.tw/cacheData/52_3.json</sourceURL>
  <column>company_name > 進口商名稱</column>
  <column>announcement_date > 發布日期</column>
  <column>content > 產地</column>
  <column>content > 主旨</column>
  <column>content > 原因</column>
  <column>content > 不合格原因暨檢出量詳細說明</column>
  <column>content > 法規限量標準</column>
  <column>provision > 處置情形</column>
  <column>region > 進口商地址</column>
  
  <subString>region > 3</subString>
</jsonData>
