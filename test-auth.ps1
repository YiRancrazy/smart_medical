# 统一认证响应测试：401 (无 token / 坏 token) / 403 (跨角色) / 200 (合法)
Add-Type -AssemblyName System.Net.Http

$client = New-Object System.Net.Http.HttpClient
$base = 'http://localhost:8080/api/admin/v1/department/list/simple/1/5'

function Get-Status {
    param([hashtable]$Headers)
    $req = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Get, $base)
    foreach ($k in $Headers.Keys) {
        $req.Headers.TryAddWithoutValidation($k, $Headers[$k]) | Out-Null
    }
    $resp = $client.SendAsync($req).GetAwaiter().GetResult()
    $body = $resp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    [pscustomobject]@{
        Status = [int]$resp.StatusCode
        Body   = $body
    }
}

# Case 1: NO-TOKEN
$r1 = Get-Status @{}
"NO-TOKEN: status=$($r1.Status) body=$($r1.Body)"

# Case 2: BAD-TOKEN
$r2 = Get-Status @{ Authorization = 'Bearer invalid_token_xxx' }
"BAD-TOKEN: status=$($r2.Status) body=$($r2.Body)"

# Case 3: 医生登录 + 访问 admin 端点
$docLogin = Invoke-RestMethod -Uri 'http://localhost:8080/api/doctor/v1/auth/login' -Method Post -ContentType 'application/json' -Body '{"phone":"15264835030","password":"123123","remember":true}'
$docToken = $docLogin.data
$r3 = Get-Status @{ Authorization = "Bearer $docToken" }
"DOC-ON-ADMIN: status=$($r3.Status) body=$($r3.Body)"

# Case 4: 管理员登录 + 访问 admin 端点
$admLogin = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/v1/auth/login' -Method Post -ContentType 'application/json' -Body '{"phone":"13996001338","password":"123456","remember":true}'
$admToken = $admLogin.data
$r4 = Get-Status @{ Authorization = "Bearer $admToken" }
"ADMIN-OK: status=$($r4.Status) body=$($r4.Body)"
