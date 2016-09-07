# OpenID Connect Liferay plugin
## 
###
###
### Sequence diagram
This diagram focuses on the interaction of typical Liferay components and components of the plugin. 
It simplifies the actual OpenID Connect authorization code flow a bit, which is documented properly in other locations.

![Sequence diagram](doc/sequence-diagram.png)

Sequence diagram rendered by https://www.websequencediagrams.com/, with source code:
~~~
title Liferay OpenID Connect authentication

Browser->Portal: GET /group/private-site/
note right of Portal
    Not authenticated
    redirect to login
end note
Portal->Browser: 302, Location: /c/portal/login
Browser->Portal: GET /c/portal/login
Portal->OpenID Connect Servlet Filter: processFilter()
note over OpenID Connect Servlet Filter, OpenID Connect Provider: OpenID Connect auth. flow, simplified
OpenID Connect Servlet Filter->OpenID Connect Provider: authorizationRequest
OpenID Connect Provider->OpenID Connect Servlet Filter: response with code
OpenID Connect Servlet Filter->OpenID Connect Provider: tokenRequest(code)
OpenID Connect Provider->OpenID Connect Servlet Filter: access token
OpenID Connect Servlet Filter->OpenID Connect Provider: get userInfo(accessToken)
OpenID Connect Provider->OpenID Connect Servlet Filter: userInfo
OpenID Connect Servlet Filter->OpenID Connect Servlet Filter: store userInfo in Session
Portal->Portal: Autologin filters
Portal->OIDCAutologin: doLogin()
note right of OIDCAutologin
    check session attrs
    create user (if DNE)
end note
OIDCAutologin->Portal: authenticated, credentials
Portal->Browser: 302, Location: /group/private-site/
Browser->Portal: GET /group/private-site/
Portal->Browser: 200 OK
~~~