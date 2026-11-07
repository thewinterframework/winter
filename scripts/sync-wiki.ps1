param(
  [Parameter(Mandatory = $true)]
  [string] $WikiDirectory
)

$ErrorActionPreference = 'Stop'

$sourceDirectory = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\docs')).Path
$targetDirectory = [System.IO.Path]::GetFullPath($WikiDirectory)
New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null

$linkPattern = '\]\((?<page>[a-z0-9-]+)\.md(?<fragment>#[^)]+)?\)'
foreach ($file in Get-ChildItem -LiteralPath $sourceDirectory -Filter '*.md' -File) {
  if ($file.Name -eq 'README.md') {
    continue
  }
  $content = Get-Content -LiteralPath $file.FullName -Raw
  $content = [regex]::Replace($content, $linkPattern, '](${page}${fragment})')
  $content = ($content -replace '(\r?\n)+$', '') + "`n"
  Set-Content -LiteralPath (Join-Path $targetDirectory $file.Name) -Value $content -Encoding utf8 -NoNewline
}

$wikiHomeContent = Get-Content -LiteralPath (Join-Path $sourceDirectory 'README.md') -Raw
$sectionStart = $wikiHomeContent.IndexOf('## Start here')
if ($sectionStart -lt 0) {
  throw 'Documentation index is missing its Start here section.'
}
$wikiHomeContent = "# Winter 3 Wiki`n`nThese pages mirror the versioned guides in the 3.0.0 branch's docs directory.`n`n" +
    $wikiHomeContent.Substring($sectionStart)
$wikiHomeContent = [regex]::Replace($wikiHomeContent, $linkPattern, '](${page}${fragment})')
$wikiHomeContent = ($wikiHomeContent -replace '(\r?\n)+$', '') + "`n"
Set-Content -LiteralPath (Join-Path $targetDirectory 'Home.md') -Value $wikiHomeContent -Encoding utf8 -NoNewline

$sidebar = @'
**Winter 3**

- [Home](Home)
- [Getting started](getting-started)
- [Configuration](configuration)
- [Architecture](architecture)
- [Components](components)
- [Lifecycle and modules](lifecycle)
- [Dependency injection](dependency-injection)
- [Exposed APIs and multi-module builds](expose-and-multi-module)
- [Conditions and decorators](conditions-and-decorators)
- [Scheduling](scheduling)
- [Paper integration](paper-and-yaml)
- [Text and utilities](utilities)
- [Extension points](extensions)
- [API map](api-reference)
- [Migration to 3.0](migration)
- [Troubleshooting](troubleshooting)
'@
Set-Content -LiteralPath (Join-Path $targetDirectory '_Sidebar.md') -Value ($sidebar + "`n") -Encoding utf8 -NoNewline

Write-Output "Synced $((Get-ChildItem -LiteralPath $targetDirectory -Filter '*.md' -File).Count) wiki pages to $targetDirectory"
