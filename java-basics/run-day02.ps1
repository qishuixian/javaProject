$ErrorActionPreference = 'Stop'
$srcDir = Join-Path $PSScriptRoot 'src'
Set-Location $srcDir
javac -encoding UTF-8 Day02TeacherData.java
java Day02TeacherData
