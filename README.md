**ci-script**
===
this project is used for lombardrisk compliance team.

**Guideline**
===
1. GenProductDPM.vbs is used on `windows`. 
    * prerequisites: check driver 'Microsoft Access Driver(*.mdb,*.accdb)' in your computer's odbc(x86 OS in C:\Windows\System32\odbcad32.exe or x64 OS in C:\Windows\SysWOW64\odbcad32.exe). 
    If it doesn't have, please install AccessDatabaseEngine.exe.
    <p>It's used for</p>
    * generating table's structures into *.ini
    * splitting tables into *.csv files
    * generating access database by these *.csv and *.ini files
    * in jenkins, we use `GenerateProductDPM.bat` to run `GenProductDPM.vbs`.
    For example, for `fed` product:
       <pre><code>ci-script\GenerateProductDPM.bat \fed\src\Metadata\FED_FORM_META.ini \fed\src\dpm\FED_FORM_META.accdb \fed\src\Metadata</code></pre>
2. zipProduct.bat is used on `windows`. It will package product files into *.zip and *.lrm.
   * in jenkins, we use `packageProduct.bat` to run zipProduct.bat.
   For example, for `fed` product:
       <pre><code>ci-script\packageProduct.bat \fed\src \fed\package.properties b110</code></pre>
3. zipProduct.sh is used on `linux`. It will package product files into *.zip and *.lrm.
   * prerequisites: need install zip, perl in Linux OS 
   * in jenkins, we use `packageProduct.sh` to run zipProduct.sh.
   For example, for `fed` product:
       <pre><code>ci-script/packageProduct.sh /fed/src /fed/package.properties b110</code></pre>
