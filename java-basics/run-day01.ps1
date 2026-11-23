$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot 'src\Day01Hello.java'
$srcDir = Join-Path $PSScriptRoot 'src'
Set-Location $srcDir
javac -encoding UTF-8 $source
java Day01Hello
