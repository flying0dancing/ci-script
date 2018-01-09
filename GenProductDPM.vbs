Option Explicit

Const adSortAscending = 1
Const adSortDescending = 2

Const adColFixed = 1
Const adColNullable = 2

Const adBigInt = 20
Const adBinary = 128
Const adBoolean = 11
Const adBSTR = 8
Const adChapter = 136
Const adChar = 129
Const adCurrency = 6
Const adDate = 7
Const adDBDate = 133
Const adDBTime = 134
Const adDBTimeStamp = 135
Const adDecimal = 14
Const adDouble = 5
Const adEmpty = 0
Const adError = 10
Const adFileTime = 64
Const adGUID = 72
Const adIDispatch = 9
Const adInteger = 3
Const adIUnknown = 13
Const adLongVarBinary = 205
Const adLongVarChar = 201
Const adLongVarWChar = 203
Const adNumeric = 131
Const adPropVariant = 138
Const adSingle = 4
Const adSmallInt = 2
Const adTinyInt = 16
Const adUnsignedBigInt = 21
Const adUnsignedInt = 19
Const adUnsignedSmallInt = 18
Const adUnsignedTinyInt = 17
Const adUserDefined = 132
Const adVarBinary = 204
Const adVarChar = 200
Const adVariant = 12
Const adVarNumeric = 139
Const adVarWChar = 202
Const adWChar = 130
Const adOpenStatic = 3  
Const adLockOptimistic = 3  
Const adCmdText = 1  

Dim dbFullName,schemaFullName,dataPath,logFile,dateTime,createDBStat
If WScript.Arguments.length=4 Then
 schemaFullName=Trim(WScript.Arguments(0))
 dbFullName=Trim(Wscript.Arguments(1))
 dataPath=Trim(Wscript.Arguments(2))
 logFile=Trim(Wscript.Arguments(3))
Else
 WScript.Echo "		HELP Document"&Chr(13)&Chr(10)&Chr(13)&Chr(10)&_
 			  "GenProductDMP.vbs [GENCSV|GENSCHEMA|schemaFullName] [accessDatabaseFullName] [dataFolder|schemaFolder] [logFullName]"&Chr(13)&Chr(10)&Chr(13)&Chr(10)&_
 			  "[GENCSV|GENSCHEMA|schemaFullName] "&Chr(13)&Chr(10)&_
 			  "	GENCSV is identified it will generate data files(*.csv) to [dataFolder]"&Chr(13)&Chr(10)&"	  according to [accessDatabaseFullName];"&Chr(13)&Chr(10)&_
 			  "	GENSCHEMA is identified it will generate schema file(*.ini) to"&Chr(13)&Chr(10)&"	  [schemaFolder] according to [accessDatabaseFullName];"&Chr(13)&Chr(10)&_
 			  "	[schemaFullName] sets to schema's full name, it will generate"&Chr(13)&Chr(10)&"	  [accessDatabaseFullName] according to [dataFolder]."&Chr(13)&Chr(10)&Chr(13)&Chr(10)&_
 			  "[accessDatabaseFullName] "&Chr(13)&Chr(10)&_
 			  "	this value is access database(*.accdb, *.mdb)'s full name."&Chr(13)&Chr(10)&Chr(13)&Chr(10)&_
 			  "[dataFolder|schemaFolder] "&Chr(13)&Chr(10)&_
 			  "	[dataFolder] sets to csv's folder;"&Chr(13)&Chr(10)&"	[schemaFolder] sets to the folder which schema will be saved in."&Chr(13)&Chr(10)&_
 			  "[logFullName] "&Chr(13)&Chr(10)&_
 			  "	this value is log's full name."&Chr(13)&Chr(10)&Chr(13)&Chr(10)&_
 			  "for example:"&Chr(13)&Chr(10)&Chr(13)&Chr(10)&_
 			  "GenProductDMP.vbs ""GENCSV"" ""E:\FED_FORM_META.accdb"" ""e:\abc"" ""e:\log\gencsv.log"" "&Chr(13)&Chr(10)&_
 			  "GenProductDMP.vbs ""GENSCHEMA"" ""E:\FED_FORM_META.accdb"" ""e:\foo"" ""e:\log\genschema.log"" "&Chr(13)&Chr(10)&_
 			  "GenProductDMP.vbs ""e:\foo\FED_FORM_META.ini"" ""E:\abc\newcreate.accdb"" ""e:\abc"" ""e:\log\gendpm.log"" "&Chr(13)&Chr(10)
 
 WScript.Quit(-1)
End If

'create log file
createlogger logFile
dateTime=Now
logger logFile,year(dateTime)&"-"&Month(dateTime)&"-"&day(dateTime)&" "&Hour(dateTime)&":"&Minute(dateTime)&":"&Second(dateTime)
If UCase(schemaFullName)="GENCSV" Then
	createFolder dataPath
	getAllTablesToCSV dbFullName,dataPath,logFile
ElseIf UCase(schemaFullName)="GENSCHEMA" Then
	createFolder dataPath
	getAllTableProps dbFullName,dataPath,logFile
Else
	Dim fso
	Set fso=CreateObject("Scripting.FileSystemObject")
	If fso.FileExists(schemaFullName) And fso.FolderExists(dataPath) Then
		createDBStat=createDatabase(dbFullName,logFile)
		If createDBStat Then
			getAllCsvIntoAccdb dbFullName,schemaFullName,dataPath,logFile
		Else 
			logger logFile,"[error] cannot create database["&dbFullName&"]"
		End If
	Else
		logger logFile,"[error] file or folder doesn't(don't) exist: schema["&schemaFullName&"],dataPath["&dataPath&"]" 
	End If
	Set fso=Nothing
End If
logger logFile,"total time(min):"&DateDiff("s",dateTime,Now)/60


If searchStr(logFile,"fail|error") Then '
  Wscript.Quit(1)
Else
  Wscript.Quit(0)
End If

'''''''''''''''''''''''''''''''''''
' list all csv file
'''''''''''''''''''''''''''''''''''
Sub getAllCsvIntoAccdb(dbFullName,schemaFullName,dataPath,logfile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"getAllCsvIntoAccdb ["& dataPath& "]"
	Dim fso,objFolder,objFile,subFolder,connStr
	connStr=getConnectStr(dbFullName)
    If isEmptyNull(connStr) Then
    	logger logFile,"connectionString is emptyString"
    	Exit Sub
    End If
    logger logFile,"connectionString is " & connStr
	Set fso=CreateObject("Scripting.FileSystemObject")
	Set objFile=CreateObject("Scripting.File")
	Set subFolder=CreateObject("Scripting.Folder")
	
	Set objFolder=fso.GetFolder(dataPath)
	Set objFiles=objFolder.Files
	
	Dim tableName,fileName
	For Each objFile In objFolder.Files
		If UCase(fso.GetExtensionName(objFile.Name))="CSV" Then
			fileName=objFile.Name
			tableName=Mid(fileName,1,InStrRev(fileName,".")-1)
			logger logFile,"	"
			logger logFile,"========================== table ["& tableName & "] =========================="
			'create table, import data
			If Not tableExistence(connStr,tableName,logFile) Then
				If createDBTable(connStr,schemaFullName,tableName,logFile) Then
					If importTextToExistTable(dbFullName,tableName,dataPath,fileName,logFile) Then
						logger logFile,"[pass] import data to existed table after create it"
					Else
						logger logFile,"[fail] import data to existed table after create it"
						'clean up objects
						Set objFile=Nothing
						Set subFolder=Nothing
						Set objFiles=Nothing
						Set objFolder=Nothing
						Set fso=Nothing
						Exit Sub
					End If
				Else 
					'cannot create table 1.cannot find schema in schema file; 2.create table fail.
					If importTxtToNewTable(dbFullName,tableName,dataPath,fileName,logFile) Then
						logger logFile,"[pass] import data to new table"
					Else
						logger logFile,"[fail] import data to new table"
						'clean up objects
						Set objFile=Nothing
						Set subFolder=Nothing
						Set objFiles=Nothing
						Set objFolder=Nothing
						Set fso=Nothing
						Exit Sub
					End If
				End If
			Else
				 
				If importTextToExistTable(dbFullName,tableName,dataPath,fileName,logFile) Then
					logger logFile,"[pass] import data to existed table"
				Else
					logger logFile,"[fail] import data to existed table"
					Exit For
				End If
			End If
			
		End If
	Next
	
	For Each subFolder In objFolder.SubFolders
		showSubFolders  connStr,schemaFullName,subFolder.Path,logFile
	Next
	
	'clean up objects
	Set objFile=Nothing
	Set subFolder=Nothing
	Set objFiles=Nothing
	Set objFolder=Nothing
	Set fso=Nothing
End Sub

'''''''''''''''''''''''''''''''''''
' list all csv file which name contains "_" in sub folders
'''''''''''''''''''''''''''''''''''
Sub showSubFolders(connStr,schemaFullName,dataPath,logfile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"showSubFolders ["& dataPath & "]"
	Dim fso,objFolder,objFile,subFolder
	Set fso=CreateObject("Scripting.FileSystemObject")
	Set objFile=CreateObject("Scripting.File")
	Set subFolder=CreateObject("Scripting.Folder")
	
	Set objFolder=fso.GetFolder(dataPath)
	Set objFiles=objFolder.Files
	
	Dim fileName,tableName,underlinePos,remindStr
	For Each objFile In objFolder.Files
		'logger logFile,"  fileName ["& objFile.Name & "]"
		fileName=objFile.Name
		If UCase(fso.GetExtensionName(fileName))="CSV" And InStr(fileName,"_")>0 Then
			underlinePos=InStr(fileName,"_")
			'WScript.Echo "  underlinePos ["& underlinePos & "]"
			tableName=left(fileName,underlinePos-1)
			remindStr=getInfoFromFileName(fileName,tableName+"_(.*)\.cSv")
			logger logFile,"	"
			logger logFile,"========================== table ["& tableName & "], return [" & remindStr &"] =========================="
			'create table, import data
			If Not tableExistence(connStr,tableName,logFile) Then
				If createDBTable(connStr,schemaFullName,tableName,logFile) Then
					If importTextToExistTable(dbFullName,tableName,dataPath,fileName,logFile) Then
						logger logFile,"[pass] import data to existed table after create it"
					Else
						logger logFile,"[fail] import data to existed table after create it"
						'clean up objects
						Set objFile=Nothing
						Set subFolder=Nothing
						Set objFiles=Nothing
						Set objFolder=Nothing
						Set fso=Nothing
						Exit Sub
					End If
				Else 
					'cannot create table 1.cannot find schema in schema file; 2.create table fail.
					If importTxtToNewTable(dbFullName,tableName,dataPath,fileName,logFile) Then
						logger logFile,"[pass] import data to new table"
					Else
						logger logFile,"[fail] import data to new table"
						'clean up objects
						Set objFile=Nothing
						Set subFolder=Nothing
						Set objFiles=Nothing
						Set objFolder=Nothing
						Set fso=Nothing
						Exit Sub
					End If
				End If
			Else
				 
				If importTextToExistTable(dbFullName,tableName,dataPath,fileName,logFile) Then
					logger logFile,"[pass] import data to existed table"
				Else
					logger logFile,"[fail] import data to existed table"
					Exit For
				End If
			End If
		End If
	Next
	
	For Each subFolder In objFolder.SubFolders
		showSubFolders connStr,schemaFullName,subFolder.Path,logfile
	Next
	
	'clean up objects
	Set objFile=Nothing
	Set subFolder=Nothing
	Set objFiles=Nothing
	Set objFolder=Nothing
	Set fso=Nothing
End Sub

'''''''''''''''''''''''''''''''''''
'import txt file into no existed table in accdb database
'''''''''''''''''''''''''''''''''''
Function importTxtToNewTable(dbFullName,tableName,dataPath,dataFileName,logFile)
	importTxtToNewTable=False
	logger logFile,"	"
	logger logFile,"import data into new ["& tableName& "] from ["& dataFileName &"]"
	Dim queryStr
	queryStr="select * into ["& tableName &"] in '" & dbFullName &"' from ["& dataFileName &"]"
    importTxtToNewTable=importTextToTable(dataPath,queryStr,logFile)
End Function

'''''''''''''''''''''''''''''''''''
'import txt file into existed table in accdb database
'''''''''''''''''''''''''''''''''''
Function importTextToExistTable(dbFullName,tableName,dataPath,dataFileName,logFile)
	importTextToExistTable=False
	logger logFile,"	"
	logger logFile,"import data into existed ["& tableName& "] from ["& dataFileName &"]"
	Dim queryStr
	queryStr="insert into ["& tableName & "] in '" & dbFullName &"' select * from ["& dataFileName &"]"
    importTextToExistTable=importTextToTable(dataPath,queryStr,logFile)
End Function

'''''''''''''''''''''''''''''''''''
'import txt file into existed table in accdb database
'''''''''''''''''''''''''''''''''''
Function importTextToTable(dataPath,queryStr,logFile)
	On Error Resume Next
	importTextToTable=False
    '   Step1
    Dim objConn,objCmd,rowcount,colcount,i
	Dim connStr,dateTime
    dateTime=Now
	connStr="Provider=Microsoft.ACE.OLEDB.12.0;" & "Data Source="&dataPath&";" & "Extended Properties=""Text;HDR=Yes;FMT=Delimited"""
	'logger logFile,"connectionString is " & connStr
	'logger logFile,"queryStr: " & queryStr
    
	Set objConn = CreateObject("ADODB.Connection")
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
        Set objConn=Nothing
        logger logFile,"escaped time(seconds):"&DateDiff("s",dateTime,Now)
        Exit Function
    End If 
     'new add
	Set objCmd=CreateObject("ADODB.COMMAND")
    objCmd.ActiveConnection=objConn
	objCmd.CommandText=queryStr
	objCmd.Execute ,,adCmdText

    If Err.Number<>0 Then
        logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objCmd.ActiveConnection.Close
        objConn.Close
        Set objCmd=Nothing
        Set objConn=Nothing
        logger logFile,"escaped time(seconds):"&DateDiff("s",dateTime,Now)
        Exit Function
    End If
 
	objCmd.ActiveConnection.Close
	objConn.Close
	Set objCmd=Nothing
	Set objConn=Nothing
	logger logFile,"escaped time(seconds):"&DateDiff("s",dateTime,Now)
	importTextToTable=True
End Function


Function isEmptyNull(str)
	isEmptyNull=True
    
	If IsEmpty(str) Then
		'WScript.Echo "[error] string is empty"
	ElseIf IsNull(str) Then
		'WScript.Echo "[error] string is null"
    ElseIf str="" Then
		'WScript.Echo "[error] string is emptyString"
	Else
		isEmptyNull=False
	End If
    
End Function

Function isNothing(str)
	isNothing=True
    If IsEmpty(str) Then
		'WScript.Echo "[error] string is empty"
	ElseIf IsNull(str) Then
		'WScript.Echo "[error] string is null"
    ElseIf (str Is Nothing) Then
        'WScript.Echo "[error] object reference is nothing"
	Else
        isNothing=False
	End If
    
End Function


Function getConnectStr(dbFullName)
	Dim extension
	extension=Mid(dbFullName,InStrRev(dbFullName,".",-1,1)+1)
	'WScript.Echo "database's extension["&extension&"]"
	If UCase(extension)="ACCDB" Then
		getConnectStr="Provider=Microsoft.ACE.OLEDB.12.0; Data Source="&Chr(34)&dbFullName &Chr(34)&";Persist Security Info=False;"
	ElseIf UCase(extension)="MDB" Then
		getConnectStr="Provider=Microsoft.Jet.OLEDB.4.0; Data Source="&Chr(34)&dbFullName &Chr(34)&";Persist Security Info=False;"
	Else
		getConnectStr=""
	End If
End Function

'''''''''''''''''''''''''''''''''''
'create Database's Table
'''''''''''''''''''''''''''''''''''
Function createDBTable(connStr,schemaFullName,tableName,logFile)
	On Error Resume Next
	createDBTable=False
	'find schema file
    If Not fileExistence(schemaFullName) Then
    	logger logFile, "[error] FileNotFind:"&schemaFullName
        Exit Function
    End If
	If searchStr(schemaFullName,"\["&tableName&"\]") Then
		logger logFile,"	"
	Else
		logger logFile,"SchemaNotDefinedIt:["&tableName&"]"
        Exit Function
	End If
	' Step1 connect
    Dim objConn,objCat,objTbl,rowcount,colcount, tableCount
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	Set objTbl=Nothing
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
        Set objConn=Nothing
        Set objCat=Nothing
        Exit Function
    End If 
    
    'objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    tableCount=objCat.Tables.Count
    logger logFile, "create table count="&tableCount
    
    'Create the table
    'add columns
    Dim fso,rh,i   
	Set fso=CreateObject("Scripting.FileSystemObject")
    Set rh=fso.OpenTextFile(schemaFullName,1)'1=ForReading
    i=1
    Do while not rh.AtEndOfStream
    	Dim line,rowStr,row,rowLen,colName,colType,colSize
    	line=rh.ReadLine
    	If line<>"" Then
    	 line=Trim(line)
    	End If
    	If line<>"" And StrComp(Left(line, 1),"[",vbTextCompare)=0 And StrComp(Right(line, 1),"]",vbTextCompare)=0 Then
    		Dim tblName
    		tblName=Mid(line, 2, Len(line)-2)
			If Not isNothing(objTbl) Then
				Exit Do
			End If
			If UCase(tableName)=UCase(tblName) Then
				'Create the table
				Set objTbl=CreateObject("ADOX.Table")
				objTbl.Name=tblName
				logger logFile,"create table ["&objTbl.Name & "]"
			End If
			i=1
		ElseIf line<>"" And Not isNothing(objTbl) Then
			Dim colNo
			colNo=left(line,InStr(line,"="))
			row=Split(Mid(line, InStr(line,"=")+1)," ",-1,vbTextCompare)
			'create column
			rowLen=UBound(row)+1
			If	rowLen=2 Or rowLen=3 Then
    			colName=row(0)
    			If InStr(row(1),"(")<=0 Then
    				colType=row(1)
    				colSize=-1
    			Else
    				colType=Mid(row(1), 1, InStr(row(1),"(")-1)
    				colSize=CLng(Mid(row(1), InStr(row(1),"(")+1, Len(row(1))-Len(colType)-2))
    			End If
			End If
			If	rowLen=4 Or rowLen=5 Then
    			colName=row(0)
    			colType=row(1)
    			colSize=CLng(row(3))
			End If
    			
    		If colSize=-1 Then
    			objTbl.Columns.Append colName,getTypeInt(colType)
    			logger logFile, colNo&colName&", type="&colType
    		Else
    			objTbl.Columns.Append colName,getTypeInt(colType),colSize
    			logger logFile, colNo&colName&", type="&colType&", size="&CStr(colSize)
    		End If
    		' set required=No
    		Dim attrInt
    		attrInt=getAttributeInt(colType)
    		If attrInt<>1 And StrComp(UCase(Right(line,8)),"NULLABLE",vbTextCompare)=0 Then
    			objTbl.Columns(colName).Attributes=attrInt
    			objTbl.Columns(colName).Properties("Nullable")=True
    		End If
    		objTbl.Columns(colName).Precision=getPrecision(colType)
    		i=i+1 
		End If
    	
	Loop
	rh.Close
	tableCount=objCat.Tables.Count
	If Not isNothing(objTbl) Then
		'append table
    	objCat.Tables.Append objTbl
    	If objCat.Tables.Count-tableCount=1 Then
			logger logFile, "[pass] create table ["&objTbl.Name &"] with "&objTbl.Columns.Count&" columns"
			'logger logFile, "create table after count="&objCat.Tables.Count
			createDBTable=True
		Else
			logger logFile, "[fail] create table ["&tableName &"]"
			createDBTable=False
		End If
	Else
		logger logFile, "[fail] create table ["&tableName &"] without schema"
		createDBTable=False
	End If
	'logger logFile, "create table after count="&objCat.Tables.Count
    objConn.Close
    'clean up objects
    Set rh=Nothing
	Set fso=Nothing
	Set objTbl=Nothing
    Set objCat=Nothing
    Set objConn=Nothing
	
End Function

'''''''''''''''''''''''''''''''''''
'create Database's Tables
'''''''''''''''''''''''''''''''''''
Sub createDBTables(connStr,schemaFullName,logFile)
	On Error Resume Next
	'find schema file
    If Not fileExistence(schemaFullName) Then
    	logger logFile, "[error] FileNotFind:"&schemaFullName
        Exit Sub
    End If
	logger logFile,"	"
	' Step1 connect
    Dim objConn,objCat,objTbl,rowcount,colcount, tableCount
    logger logFile,"connectionString is " & connStr
    
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	'Set objTbl=CreateObject("ADOX.Table")
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
        Set objConn=Nothing
        Set objCat=Nothing
        Exit Sub
    End If 
    
    'objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    tableCount=objCat.Tables.Count
    logger logFile, "before create tables count="&tableCount
    
    'Create the table
    'add columns
    Dim fso,rh,i,tblName
	Set fso=CreateObject("Scripting.FileSystemObject")
    Set rh=fso.OpenTextFile(schemaFullName,1)'1=ForReading
    i=1
    Do while not rh.AtEndOfStream
    	Dim line,rowStr,row,rowLen,colName,colType,colSize
    	line=rh.ReadLine
    	If line<>"" Then
    	 line=Trim(line)
    	End If
    	If line<>"" And StrComp(Left(line, 1),"[",vbTextCompare)=0 And StrComp(Right(line, 1),"]",vbTextCompare)=0 Then
    		tblName=Mid(line, 2, Len(line)-2)
			If StrComp(objTbl.Name,tblName,vbTextCompare)<>0 Then
				tableCount=objCat.Tables.Count
				'append table
				objCat.Tables.Append objTbl
				If objCat.Tables.Count-tableCount=1 Then
					logger logFile, "[pass] create table ["&objTbl.Name &"] with "&objTbl.Columns.Count&" columns"
				Else
					logger logFile, "[fail] create table ["&tblName &"]"
				End If
			End If
			'delete existed table
			Call deleteDBTable(connStr,tblName,logFile)
				
			'Create the table
			Set objTbl=CreateObject("ADOX.Table")
			objTbl.Name=tblName
			logger logFile,"create table ["&objTbl.Name & "]"
			i=1
		ElseIf line<>"" Then
			Dim colNo
			colNo=left(line,InStr(line,"="))
			row=Split(Mid(line, InStr(line,"=")+1)," ",-1,vbTextCompare)
			'create column
			rowLen=UBound(row)+1
			If	rowLen=2 Or rowLen=3 Then
    			colName=row(0)
    			If InStr(row(1),"(")<=0 Then
    				colType=row(1)
    				colSize=-1
    			Else
    				colType=Mid(row(1), 1, InStr(row(1),"(")-1)
    				colSize=CLng(Mid(row(1), InStr(row(1),"(")+1, Len(row(1))-Len(colType)-2))
    			End If
			End If
			If	rowLen=4 Or rowLen=5 Then
    			colName=row(0)
    			colType=row(1)
    			colSize=CLng(row(3))
			End If
    			
    		If colSize=-1 Then
    			objTbl.Columns.Append colName,getTypeInt(colType)
    			logger logFile, colNo&colName&", type="&colType
    		Else
    			objTbl.Columns.Append colName,getTypeInt(colType),colSize
    			logger logFile, colNo&colName&", type="&colType&", size="&CStr(colSize)
    		End If
    		' set required=No
    		Dim attrInt
    		attrInt=getAttributeInt(colType)
    		If attrInt<>1 And StrComp(UCase(Right(line,8)),"NULLABLE",vbTextCompare)=0 Then
    			objTbl.Columns(colName).Attributes=attrInt
    			objTbl.Columns(colName).Properties("Nullable")=True
    		End If
    		objTbl.Columns(colName).Precision=getPrecision(colType)
    		i=i+1 
		End If
    	
	Loop 
	rh.Close
	tableCount=objCat.Tables.Count
    objCat.Tables.Append objTbl
    If objCat.Tables.Count-tableCount=1 Then
		logger logFile, "[pass] create table ["&objTbl.Name &"] with "&objTbl.Columns.Count&" columns"
	Else
		logger logFile, "[fail] create table ["&tblName &"]"
	End If
	
  	logger logFile, "after create tables count="&objCat.Tables.Count
    objConn.Close
    'clean up objects
    Set rh=Nothing
	Set fso=Nothing
    Set objTbl=Nothing
    Set objCat=Nothing
    Set objConn=Nothing
	
End Sub


'''''''''''''''''''''''''''''''''''
'get column's properties ofDatabase's Table, disorder
'''''''''''''''''''''''''''''''''''
Sub getAllTablePropsDisorder(connStr,logFile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"get all tables properties "
	' Step1 connect
    Dim objConn,objCat,tblTmp,colTmp,propTmp,i,j,colStr
    logger logFile,"get all tables properties connectionString is " & connStr
    
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
    	Set objCat=Nothing
    	Set objConn=Nothing
        Exit Sub
    End If 
    
    objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    Set tblTmp=CreateObject("ADOX.Table")
    
    For Each tblTmp In objCat.Tables
    	'logger logFile,"[" & tblTmp.Name & "]"&"{" & tblTmp.Type & "}"
    	If UCase(Mid(tblTmp.Name,1,4))<>"MSYS" And UCase(tblTmp.Type)="TABLE" Then
    	logger logFile,"[" & tblTmp.Name & "]"
    	
    	Set colTmp=CreateObject("ADOX.Column")
		'Set propTmp=CreateObject("ADOX.Property")
		For i=0 To tblTmp.Columns.Count-1
			Set colTmp=tblTmp.Columns.Item(i)
			If colTmp.Type=7  Or colTmp.Type=202 Or colTmp.Type=203 Or colTmp.Type=5 Or colTmp.Type=131 Then 
				
			Else
				logger logFile,"========colTmpType:"&colTmp.Type  &", attribute=" & CStr(colTmp.Attributes)
			End If
			'
    		colStr="col"& i+1 &"=" & colTmp.Name & " "& getTypeStr(colTmp.Type,colTmp.DefinedSize) '&" "&colTmp.DefinedSize & " "&colTmp.Precision & " " & colTmp.NumericScale
    		If colTmp.Properties("Nullable").Value=True Then
    			colStr=colStr & " Nullable"
    		End If
    		logger logFile,colStr
		Next
		
    	'clean up objects
    	Set colTmp=Nothing
    	'Set propTmp=Nothing
    	End If
    Next
    'clean up objects
    Set tblTmp=Nothing
    
    If Err.Number<>0 Then
        logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
       	Set objCat=Nothing
    	Set objConn=Nothing
        Exit Sub
    End If 
    
    objConn.Close
    'clean up objects
    Set colTmp=Nothing
    Set propTmp=Nothing
    Set tblTmp=Nothing
    Set objCat=Nothing
    Set objConn=Nothing
End Sub

'''''''''''''''''''''''''''''''''''
'get column's properties ofDatabase's Table, ordered
'''''''''''''''''''''''''''''''''''
Sub getAllTableProps(dbFullName,schemaPath,logFile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"get all tables schema "
	' Step1 connect
    Dim objConn,objCat,tblTmp,colTmp,i,j,colStr,connStr
    Dim objCmd,objRS,strQuery,objConn2
    Dim schemaFile,schemaName
    schemaName=Mid(dbFullName,InStrRev(dbFullName,"\")+1,InStrRev(dbFullName,".")-InStrRev(dbFullName,"\")-1)
    If Right(schemaPath,1)="\" Then
    	schemaFile=schemaPath&schemaName&".ini"
    Else
    	schemaFile=schemaPath&"\"&schemaName&".ini"
    End If
    logger logFile,"get all tables schema save to " & schemaFile
    createFile schemaFile
    connStr=getConnectStr(dbFullName)
    logger logFile,"get all tables schema connectionString is " & connStr
    
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
    	Set objCat=Nothing
    	Set objConn=Nothing
        Exit Sub
    End If 
    
    objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    Set tblTmp=CreateObject("ADOX.Table")
    
    For Each tblTmp In objCat.Tables
    	'logger logFile,"[" & tblTmp.Name & "]"&"{" & tblTmp.Type & "}"
    	If UCase(Mid(tblTmp.Name,1,4))<>"MSYS" And UCase(tblTmp.Type)="TABLE"  Then
    	logger logFile,"[" & tblTmp.Name & "]"
    	logger schemaFile,"[" & tblTmp.Name & "]"
    	'new add
    	Set objConn2 = CreateObject("ADODB.Connection")
		Set objCmd=CreateObject("ADODB.COMMAND")
		Set objRS=CreateObject("ADODB.Recordset")
		objConn2.Open connStr
    	objCmd.ActiveConnection=objConn2
    	strQuery="select top 1 * from ["&tblTmp.Name&"]"
		objCmd.CommandText=strQuery
		objRS.CursorLocation=3
		objRS.Open objCmd
		
		If Err.Number<>0 Then
        	logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        	Err.Clear
       		objRS.Close	
			'objCmd.ActiveConnection.Close
			objConn2.Close
			Set objCmd=Nothing
    		Set objRS=Nothing
    		Set objConn2=Nothing
        Else
        
        	Set colTmp=CreateObject("ADOX.Column")
			For j=0 To objRS.Fields.Count-1
				Set colTmp=objRS.Fields(j)
				colStr="col" & j+1 &"="& colTmp.Name&" "& getTypeStr(colTmp.Type,colTmp.DefinedSize) '&" "&colTmp.DefinedSize & " "&colTmp.Precision & " " & colTmp.NumericScale
				If colTmp.Properties("Nullable").Value=True Then
    				colStr=colStr & " Nullable"
    			End If
    			logger logFile,colStr
    			logger schemaFile,colStr
			Next
			'clean up objects
			Set colTmp=Nothing
        	
			'clean up objects
			objRS.Close	
			'objCmd.ActiveConnection.Close
			objConn2.Close
			Set objCmd=Nothing
    		Set objRS=Nothing
    		Set objConn2=Nothing
    	End If 
    
    	End If
    Next
    'clean up objects
    Set tblTmp=Nothing
    
     If Err.Number<>0 Then
        	logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        	Err.Clear
       		objRS.Close	
			'objCmd.ActiveConnection.Close
			objConn2.Close
			Set objCmd=Nothing
    		Set objRS=Nothing
    		Set objConn2=Nothing
    End If 
    
    objConn.Close
    'clean up objects
    Set colTmp=Nothing
    Set tblTmp=Nothing
    Set objCat=Nothing
    Set objConn=Nothing
End Sub

'''''''''''''''''''''''''''''''''''
'get column's properties ofDatabase's Table,disorder
'''''''''''''''''''''''''''''''''''
Function getDBColumnProps(connStr,tableName,logFile)
	On Error Resume Next
	getDBColumnProps=False
	logger logFile,"	"
	logger logFile,"get column properties [" & tableName & "]"
	' Step1 connect
    Dim objConn,objCat,colTmp,propTmp
    logger logFile,"get column properties connectionString is " & connStr
    
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
    	Set objCat=Nothing
    	Set objConn=Nothing
        Exit Function
    End If 
    
    'objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    
    If tableExistence(connStr,tableName,logFile) Then
    	WScript.Echo "table existed" & objCat.Tables(tableName).Columns.Count
    	Set colTmp=CreateObject("ADOX.Column")
		Set propTmp=CreateObject("ADOX.Property")
    	For Each colTmp In objCat.Tables(tableName).Columns
    		logger logFile,"  col: name=" & colTmp.Name & ", type="& colTmp.Type & ", attribute=" & CStr(colTmp.Attributes)&" DefinedSize:"&colTmp.DefinedSize & " Precision:"&colTmp.Precision & " NumericScale:" & colTmp.NumericScale
    		'For Each propTmp In colTmp.Properties 
    			'logger logFile,"	property: name=" & propTmp.Name & ", type=" & propTmp.Type & ", value=" & propTmp.Value 
    		'Next
    	Next
    	For Each colTmp In objCat.Tables(tableName).Columns
    		logger logFile,"  col: name=" & colTmp.Name & ", type="& colTmp.Type & ", attribute=" & CStr(colTmp.Attributes)&" DefinedSize:"&colTmp.DefinedSize & " Precision:"&colTmp.Precision & " NumericScale:" & colTmp.NumericScale
    		For Each propTmp In colTmp.Properties 
    			logger logFile,"	property: name=" & propTmp.Name & ", type=" & propTmp.Type & ", value=" & propTmp.Value 
    		Next
    	Next
    	getDBColumnProps=True
    	'clean up objects
    	Set colTmp=Nothing
    	Set propTmp=Nothing
    End If
    If Err.Number<>0 Then
        logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
       	Set objCat=Nothing
    	Set objConn=Nothing
        Exit Function
    End If 
    
    objConn.Close
    'clean up objects
    Set objCat=Nothing
    Set objConn=Nothing
End Function

'''''''''''''''''''''''''''''''''''
'delete Database's Table
'''''''''''''''''''''''''''''''''''
Function deleteDBTable(connStr,tableName,logFile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"delete table [" & tableName & "]"
	' Step1 connect
    Dim objConn,objCat,tableCountbefore,tableCountAfter
	
    logger logFile,"delete table connectionString is " & connStr
    
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
        deleteDBTable=false
        Set objConn=Nothing
        Exit Function
    End If 
    
    'objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    
    tableCountbefore= objCat.Tables.Count
    objCat.Tables.Delete(tableName)
    tableCountAfter=objCat.Tables.Count
    If tableCountbefore-tableCountAfter=1 Then
    	logger logFile,"[pass] delete table [" & tableName & "]"
     	deleteDBTable=True
    Else
    	logger logFile,"[fail] delete table [" & tableName & "]"
     	deleteDBTable=False
    End If
    objConn.Close
    'clean up objects
    Set objCat=Nothing
    Set objConn=Nothing
End Function

'''''''''''''''''''''''''''''''''''
'database's table existence
'''''''''''''''''''''''''''''''''''
Function tableExistence(connStr,tableName,logFile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"find table ["&tableName&"]"
    Dim db,rs,exist
    tableExistence=False
    Set db = CreateObject("ADODB.Connection")
    Set rs=CreateObject("ADODB.Recordset")
    db.Open connStr
    Set rs=db.OpenSchema(20) 'adSchemaTables=20
    Do While Not rs.EOF
    	'logger logFile,"finding table ["&rs("TABLE_NAME")&"]"
    	If Chr(34)&rs("TABLE_NAME")&Chr(34)=Chr(34)&tableName&Chr(34) Then
    		tableExistence=True
    		logger logFile,"[info] table ["&tableName&"] exists"
    		Exit Do
    	End If
    	rs.MoveNext
    Loop
    rs.Close
    db.Close
    If Not tableExistence Then
    	logger logFile,"[info] table ["&tableName&"] not found"
    End If
    Set rs=Nothing
    Set db=Nothing
End Function

'''''''''''''''''''''''''''''''''''
'create Database
'''''''''''''''''''''''''''''''''''
Function createDatabase(dbFullName,logFile)
	On Error Resume Next
	createDatabase=False
	logger logFile,"	"
	logger logFile,"createDatabase ["&dbFullName&"]"
	'find db file
    If fileExistence(dbFullName) Then
    	logger logFile, "[info] FileExisted:"&dbFullName
    	Call deleteFile(dbFullName)
    Else
    	logger logFile, "[info] FileNotFind:"&dbFullName
    End If
    Dim dbPath
    dbPath=Mid(dbFullName,1,InStrRev(dbFullName,"\"))
    createFolder dbPath
	' Step1 
    Dim objCat,connStr,extension
	extension=Mid(dbFullName,InStrRev(dbFullName,".",-1,1)+1)
	If UCase(extension)="ACCDB" Then
		connStr="Provider=Microsoft.ACE.OLEDB.12.0; Data Source="&Chr(34)&dbFullName &Chr(34)&";"
	ElseIf UCase(extension)="MDB" Then
		connStr="Provider=Microsoft.Jet.OLEDB.4.0; Data Source="&Chr(34)&dbFullName &Chr(34)&";"
	Else
		logger logFile,"connectionString is not generated"
    	Exit Function
	End If
    logger logFile,"connectionString is " & connStr
    
	Set objCat=CreateObject("ADOX.Catalog")
	objCat.Create(connStr)
	
    If Err.Number<>0 Then
        logger logFile,"[fail] createDatabase [" & dbFullName & "]"
        Err.Clear
        Set objCat=Nothing
        Exit Function
    Else
    	logger logFile,"[pass] createDatabase [" & dbFullName & "]"
    	createDatabase=True
    End If 
    
    'clean up objects
    Set objCat=Nothing
End Function

'''''''''''''''''''''''''''''''''''
'if full file name exist,delete it
'''''''''''''''''''''''''''''''''''
Function deleteFile(fileFullName)
	On Error Resume Next
	deleteFile=False
	Dim fso
	Set fso=CreateObject("Scripting.FileSystemObject")
	If fso.FileExists(fileFullName) Then
		fso.DeleteFile(fileFullName)
		deleteFile=True
	End If
	Set fso=Nothing
End Function

'''''''''''''''''''''''''''''''''''
'if full file name exist
'''''''''''''''''''''''''''''''''''
Function fileExistence(fileFullName)
	On Error Resume Next
	fileExistence=False
	Dim fso
	Set fso=CreateObject("Scripting.FileSystemObject")
	If fso.FileExists(fileFullName) Then
		fileExistence=True
	End If
	Set fso=Nothing
End Function


'''''''''''''''''''''''''''''''''''
' create log file
'''''''''''''''''''''''''''''''''''
Function createlogger(logfile)
	On Error Resume Next
	Dim fso,wh
	Set fso=CreateObject("Scripting.FileSystemObject")
	If fso.FileExists(logfile) Then
		fso.DeleteFile(logFile)
	End If
	Dim logPath
	logPath=Mid(logFile,1,InStrRev(logFile,"\"))
	If createFolder(logPath) Then
		fso.CreateTextFile(logFile)
		Set wh=fso.OpenTextFile(logfile,2)'2=ForWriting
		wh.WriteLine("''''''''''''''''''''' log info '''''''''''''''''''''")
		wh.Close
	End If
	createlogger=fso.FileExists(logfile)
	Set wh=Nothing
	Set fso=Nothing
End Function

'''''''''''''''''''''''''''''''''''
' create file
'''''''''''''''''''''''''''''''''''
Function createFile(logfile)
	On Error Resume Next
	Dim fso
	Set fso=CreateObject("Scripting.FileSystemObject")
	If fso.FileExists(logfile) Then
		fso.DeleteFile(logFile)
	End If
	Dim logPath
	logPath=Mid(logFile,1,InStrRev(logFile,"\"))
	If createFolder(logPath) Then
		fso.CreateTextFile(logFile)
	End If
	createFile=fso.FileExists(logfile)

	Set fso=Nothing
End Function


'''''''''''''''''''''''''''''''''''
' create folder
'''''''''''''''''''''''''''''''''''
Function createFolder(logFolder)
	WScript.Echo "createFolder"&logFolder
	On Error Resume Next
	Dim fso,pathArr,pathstr,pat,i
	Set fso=CreateObject("Scripting.FileSystemObject")
	If fso.FolderExists(logFolder) Then
		createFolder=True
		Set fso=Nothing
		Exit Function
	Else
		pat="\"
		pathstr=""
		pathArr=Split(logFolder,pat)
		For i=1 To UBound(pathArr)-1
			If pathArr(i)<>"" Then
				pathstr=pathstr&pat&pathArr(i)
				If Not fso.FolderExists(pathArr(0)&pat&pathstr) Then
					fso.CreateFolder(pathArr(0)&pat&pathstr)
				End If
			End If
		Next
		
	End If
	createFolder=fso.FolderExists(logFolder)
	
	Set fso=Nothing
End Function

'''''''''''''''''''''''''''''''''''
'write msg to log file
'''''''''''''''''''''''''''''''''''
Sub logger(logfile,msg)
	On Error Resume Next
	WScript.Echo msg
	Dim fso,wh
	Set fso=CreateObject("Scripting.FileSystemObject")
	Set wh=fso.OpenTextFile(logfile,8)'2=ForWriting
	wh.WriteLine(msg)
	wh.Close
	Set wh=Nothing
	Set fso=Nothing
End Sub

'''''''''''''''''''''''''''''''''''
'get type code by type's string
'''''''''''''''''''''''''''''''''''
Function getTypeInt(typeString)
	Dim typeStr
	getTypeInt=adVarWChar
	typeStr=UCase(typeString)
	'WScript.Echo typeStr
	If	StrComp(typeStr,"VARCHAR" ,vbTextCompare)=0 Then
		getTypeInt= adVarWChar '202
	ElseIf StrComp(typeStr,"LONGTEXT" ,vbTextCompare)=0 Then
		getTypeInt=adLongVarWChar  '203
	ElseIf	StrComp(typeStr,"DATE" ,vbTextCompare)=0 Then
		getTypeInt=adDate  '7
	ElseIf	StrComp(typeStr,"LONG" ,vbTextCompare)=0 Or StrComp(typeStr,"AUTONUMBER" ,vbTextCompare)=0 Then
		getTypeInt=adInteger  '3
	ElseIf	StrComp(typeStr,"INTEGER" ,vbTextCompare)=0 Then
		getTypeInt=adSmallInt  '2
	ElseIf	StrComp(typeStr,"SINGLE" ,vbTextCompare)=0 Then
		getTypeInt=adSingle  '4
	ElseIf	StrComp(typeStr,"DOUBLE" ,vbTextCompare)=0 Then
		getTypeInt=adDouble  '5
	ElseIf	StrComp(typeStr,"DECIMAL" ,vbTextCompare)=0 Then
		getTypeInt=adNumeric '131
	End If

End Function

'''''''''''''''''''''''''''''''''''
'get attribute code by type's string
'''''''''''''''''''''''''''''''''''
Function getAttributeInt(typeString)
	Dim typeStr
	getAttributeInt=2
	typeStr=UCase(typeString)
	'WScript.Echo typeStr
	If	StrComp(typeStr,"VARCHAR" ,vbTextCompare)=0 Then
		getAttributeInt=2 '202
	ElseIf StrComp(typeStr,"LONGTEXT" ,vbTextCompare)=0 Then
		getAttributeInt=2  '203
	ElseIf	StrComp(typeStr,"DATE" ,vbTextCompare)=0 Then
		getAttributeInt=3  '7
	ElseIf	StrComp(typeStr,"LONG" ,vbTextCompare)=0 Then
		getAttributeInt=3  '3
	ElseIf	StrComp(typeStr,"AUTONUMBER" ,vbTextCompare)=0 Then
		getAttributeInt=1  '3
	ElseIf	StrComp(typeStr,"INTEGER" ,vbTextCompare)=0 Then
		getAttributeInt=3  '2
	ElseIf	StrComp(typeStr,"SINGLE" ,vbTextCompare)=0 Then
		getAttributeInt=3  '4
	ElseIf	StrComp(typeStr,"DOUBLE" ,vbTextCompare)=0 Then
		getAttributeInt=3  '5
	ElseIf	StrComp(typeStr,"DECIMAL" ,vbTextCompare)=0 Then
		getAttributeInt=3  '131
	End If

End Function

'''''''''''''''''''''''''''''''''''
'get precision code by type's string
'''''''''''''''''''''''''''''''''''
Function getPrecision(typeString)
	Dim typeStr
	getPrecision=0
	typeStr=UCase(typeString)
	'WScript.Echo typeStr
	If	StrComp(typeStr,"VARCHAR" ,vbTextCompare)=0 Then
		getPrecision=0 '202
	ElseIf StrComp(typeStr,"LONGTEXT" ,vbTextCompare)=0 Then
		getPrecision=0  '203
	ElseIf	StrComp(typeStr,"DATE" ,vbTextCompare)=0 Then
		getPrecision=0  '7
	ElseIf	StrComp(typeStr,"LONG" ,vbTextCompare)=0 Or StrComp(typeStr,"AUTONUMBER" ,vbTextCompare)=0 Then
		getPrecision=10  '3
	ElseIf	StrComp(typeStr,"INTEGER" ,vbTextCompare)=0 Then
		getPrecision=5  '2
	ElseIf	StrComp(typeStr,"SINGLE" ,vbTextCompare)=0 Then
		getPrecision=7  '4
	ElseIf	StrComp(typeStr,"DOUBLE" ,vbTextCompare)=0 Then
		getPrecision=15  '5
	ElseIf	StrComp(typeStr,"DECIMAL" ,vbTextCompare)=0 Then
		getPrecision=18  '131
	End If

End Function

'''''''''''''''''''''''''''''''''''
'get type's string with default size by type code and default size
'''''''''''''''''''''''''''''''''''
Function getTypeStr(typeInt,defsize)
	getTypeStr="VARCHAR("&defsize&")"
	'WScript.Echo typeStr
	If typeInt=202 Then
		getTypeStr= "VARCHAR("&defsize&")" '202
	ElseIf typeInt=203 Then
		getTypeStr= "LONGTEXT"  '203
	ElseIf typeInt=7 Then
		getTypeStr= "DATE"   '7
	ElseIf	typeInt=3 Then
		getTypeStr= "LONG"  '3
	ElseIf	typeInt=2 Then
		getTypeStr= "INTEGER"  '2
	ElseIf	typeInt=4 Then
		getTypeStr= "SINGLE"  '4
	ElseIf	typeInt=5 Then
		getTypeStr= "DOUBLE"  '5
	ElseIf	typeInt=131 Then
		getTypeStr= "DECIMAL"  '131
	End If
End Function

'''''''''''''''''''''''''''''''''''
'get csv file's infomation
'''''''''''''''''''''''''''''''''''
Function getInfoFromFileName(strng,patrn)
   Dim regEx ,match,matches ,retStr  ' 建立变量。
   Set regEx = New RegExp   ' 建立规范表达式。
   regEx.Pattern = patrn   ' 设置模式。
   regEx.IgnoreCase = True   ' 设置是否区分字母的大小写。
   regEx.Global = True   ' 设置全程性质。
   set matches= regEx.Execute(strng)   ' 执行搜索。
   for each match in matches      ' 重复匹配集合
      retStr=retStr & match.Submatches(0)
   Next
   getInfoFromFileName=retStr
   Set regEx =Nothing
End Function

'''''''''''''''''''''''''''''''''''
'search something like a string in a file's content
'''''''''''''''''''''''''''''''''''
Function searchStr(fileFullName,patrn)
   searchStr=False
   Dim line,fso,rh,regEx
   Set regEx = New RegExp 
   regEx.Pattern = patrn
   regEx.IgnoreCase = True
   regEx.Global = True
	Set fso=CreateObject("Scripting.FileSystemObject")
    Set rh=fso.OpenTextFile(fileFullName,1)'1=ForReading
    
    Do While Not rh.AtEndOfStream
    	line=rh.ReadLine
    	If line<>"" Then
    		line=Trim(line)
    		If regEx.Test(line) Then
    			searchStr=True
    			Exit Do	
    		End If
    	End If
    Loop
   
   rh.Close
   Set fso=Nothing
   Set rh=Nothing
   
End Function


'''''''''''''''''''''''''''''''''''
'array contains it or not
'''''''''''''''''''''''''''''''''''
Function containsInArr(arr,str)
	containsInArr=False
	Dim astr
	For Each astr In arr
		If astr=str Then
			containsInArr=True
			Exit For
		End If
	Next
End Function


'''''''''''''''''''''''''''''''''''
'get all tables's data, save to csv file
'''''''''''''''''''''''''''''''''''
Sub getAllTablesToCSV(dbFullName,dataPath,logFile)
	On Error Resume Next
	logger logFile,"	"
	logger logFile,"get all tables properties "
	' Step1 connect
    Dim objConn,objCat,tblTmp,i,j,colStr,connStr,arr
    createFolder dataPath
    If Right(dataPath,1)<>"\" Then
    	dataPath=dataPath&"\"
    End If
    connStr=getConnectStr(dbFullName)
    logger logFile,"get all tables properties connectionString is " & connStr
    'tables in this array will be split into several csv
	arr=Array("GridKey","GridRef","List","Ref","Sums","Vals","XVals")
	
	Set objConn = CreateObject("ADODB.Connection")
	Set objCat=CreateObject("ADOX.Catalog")
	
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
    	Set objCat=Nothing
    	Set objConn=Nothing
        Exit Sub
    End If 
    
    objCat.Create(connStr)
    Set objCat.ActiveConnection=objConn
    Set tblTmp=CreateObject("ADOX.Table")
    	
    For Each tblTmp In objCat.Tables
    	If UCase(Mid(tblTmp.Name,1,4))<>"MSYS" And UCase(tblTmp.Type)="TABLE"  Then
    		logger logFile,"	"
    		logger logFile,"[" & tblTmp.Name & "]"
    		If containsInArr(arr,tblTmp.Name) Then
    			createFolder(dataPath & tblTmp.Name)
    			If splitTableDataToMoreCSVs(connStr,tblTmp.Name,dataPath & tblTmp.Name,logFile) Then
    				logger logFile,"[pass] to split data to files "& tblTmp.Name &"_*.csv"
    			Else
    				logger logFile,"[fail] to split data to files "& tblTmp.Name &"_*.csv"
    			End If
    		Else
    			If importTableToNewText(connStr,tblTmp.Name,dataPath,tblTmp.Name&".csv","",logFile) Then
    				logger logFile,"[pass] to import data to file "& tblTmp.Name &".csv"
    			Else
    				logger logFile,"[fail] to import data to file "& tblTmp.Name &".csv"
    			End If
    		End If
		
    	End If
    Next
    
    objConn.Close
    'clean up objects
    Set tblTmp=Nothing
    Set objCat=Nothing
    Set objConn=Nothing
End Sub

'''''''''''''''''''''''''''''''''''
'split table's data to several csv files
'''''''''''''''''''''''''''''''''''
Function splitTableDataToMoreCSVs(connStr,tableName,dataPath,logFile )
	splitTableDataToMoreCSVs=False
	Dim objConn,objCmd,objRS,strQuery,rowcount,colcount,i,identifierArr,dataFileName,stat
	logger logFile,"	"
	createFolder dataPath
	If Right(dataPath,1)<>"\" Then
    	dataPath=dataPath&"\"
    End If
    	'new add
    	Set objConn = CreateObject("ADODB.Connection")
    	objConn.Open connStr
    	
		Set objCmd=CreateObject("ADODB.COMMAND")
		Set objRS=CreateObject("ADODB.Recordset")
    	objCmd.ActiveConnection=objConn
    	strQuery="select distinct  ReturnId from ["&tableName&"]"
		objCmd.CommandText=strQuery
		objRS.CursorLocation=3
		objRS.Open objCmd
		
		If Err.Number<>0 Then
        	logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        	Err.Clear
       		objRS.Close	
			objCmd.ActiveConnection.Close
			objConn.Close
			Set objCmd=Nothing
    		Set objRS=Nothing
    		Set objConn=Nothing
        	Exit Function
    	End If 
    	rowcount=objRS.RecordCount
    	colcount=objRS.Fields.Count
    	i=0
    	reDim identifierArr(rowcount)
        Do While Not objRS.EOF
        	identifierArr(i)=objRS.Fields(0).Value
        	i=i+1	
        	objRS.MoveNext
        Loop
        'clean up objects
		objRS.Close	
		objConn.Close
		Set objCmd=Nothing
    	Set objRS=Nothing
    	Set objConn=Nothing
    	stat=True
		For i=0 To ubound(identifierArr)-1
			logger logFile, "returnId="&identifierArr(i) ' returnId
			dataFileName=tableName&"_"&identifierArr(i)&".csv"
			stat=importTableToNewText(connStr,tableName,dataPath,dataFileName,identifierArr(i),logFile)
            If stat=False Then
            	logger logFile,"[error]	stop to split data into csv, stop at file name "& dataFileName
            	Exit For
            End If
		Next
     	If stat=True Then
     		splitTableDataToMoreCSVs=True
     	End If
End Function
'''''''''''''''''''''''''''''''''''
'import txt file into no existed table in accdb database
'''''''''''''''''''''''''''''''''''
Function importTableToNewText(connStr,tableName,dataPath,dataFileName,returnId,logFile)
	importTableToNewText=False
	logger logFile,"	"
	logger logFile,"import data into new ["& dataFileName& "] from ["& tableName &"]"
	createFolder dataPath
	If Right(dataPath,1)<>"\" Then
    	dataPath=dataPath&"\"
    End If
	Dim queryStr
	If returnId="" Then
		queryStr="select *  into [Text;HDR=Yes;Database="&dataPath&"].["&dataFileName&"] from ["& tableName &"]"
	Else
		queryStr="select *  into [Text;HDR=Yes;Database="&dataPath&"].["&dataFileName&"] from ["& tableName &"] where ReturnId='"&returnId&"'"
	End If
	importTableToNewText=importTableToText(connStr,queryStr,logFile)
End Function

'''''''''''''''''''''''''''''''''''
'import table to text file
'''''''''''''''''''''''''''''''''''
Function importTableToText(connStr,queryStr,logFile)
	On Error Resume Next
	importTableToText=False
	'logger logFile,"import data into ["& tableName& "]"
    '   Step1
    Dim objConn,objCmd,rowcount,colcount,i
	
	Set objConn = CreateObject("ADODB.Connection")
	objConn.Open connStr
    If Err.Number<>0 Then
        logger logFile, "[error] connect:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objConn.Close
        Set objConn=Nothing
        Exit Function
    End If 
    
    'new add
	Set objCmd=CreateObject("ADODB.COMMAND")
    objCmd.ActiveConnection=objConn
	objCmd.CommandText=queryStr
	objCmd.Execute ,,adCmdText

    If Err.Number<>0 Then
        logger logFile, "[error] operate:"&CStr(Err.Number)& Err.Description & vbCrLf & Err.Source
        Err.Clear
        objCmd.ActiveConnection.Close
        objConn.Close
        Set objCmd=Nothing
        Set objConn=Nothing
        Exit Function
    End If
 
	objCmd.ActiveConnection.Close
	objConn.Close
	Set objCmd=Nothing
	Set objConn=Nothing
	importTableToText=True
End Function