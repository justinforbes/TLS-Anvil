"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[811],{3905:function(e,t,n){n.d(t,{Zo:function(){return u},kt:function(){return m}});var r=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function o(e,t){if(null==e)return{};var n,r,i=function(e,t){if(null==e)return{};var n,r,i={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var s=r.createContext({}),c=function(e){var t=r.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},u=function(e){var t=c(e.components);return r.createElement(s.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},h=r.forwardRef((function(e,t){var n=e.components,i=e.mdxType,a=e.originalType,s=e.parentName,u=o(e,["components","mdxType","originalType","parentName"]),h=c(n),m=i,d=h["".concat(s,".").concat(m)]||h[m]||p[m]||a;return n?r.createElement(d,l(l({ref:t},u),{},{components:n})):r.createElement(d,l({ref:t},u))}));function m(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var a=n.length,l=new Array(a);l[0]=h;var o={};for(var s in t)hasOwnProperty.call(t,s)&&(o[s]=t[s]);o.originalType=e,o.mdxType="string"==typeof e?e:i,l[1]=o;for(var c=2;c<a;c++)l[c]=n[c];return r.createElement.apply(null,l)}return r.createElement.apply(null,n)}h.displayName="MDXCreateElement"},3274:function(e,t,n){n.r(t),n.d(t,{assets:function(){return u},contentTitle:function(){return s},default:function(){return m},frontMatter:function(){return o},metadata:function(){return c},toc:function(){return p}});var r=n(7462),i=n(3366),a=(n(7294),n(3905)),l=["components"],o={},s="Client Testing",c={unversionedId:"Quick-Start/Client-Testing",id:"Quick-Start/Client-Testing",title:"Client Testing",description:"This site demonstrates how to test the OpenSSL client provided by the TLS-Docker-Library.",source:"@site/docs/01-Quick-Start/02-Client-Testing.md",sourceDirName:"01-Quick-Start",slug:"/Quick-Start/Client-Testing",permalink:"/docs/Quick-Start/Client-Testing",draft:!1,editUrl:"https://github.com/tls-attacker/TLS-Anvil/tree/main/Docs/docs/01-Quick-Start/02-Client-Testing.md",tags:[],version:"current",sidebarPosition:2,frontMatter:{},sidebar:"tutorialSidebar",previous:{title:"Server Testing",permalink:"/docs/Quick-Start/Server-Testing"},next:{title:"Result Analysis",permalink:"/docs/Quick-Start/Result-Analysis"}},u={},p=[{value:"Preperations",id:"preperations",level:3},{value:"Starting the TLS-Anvil container",id:"starting-the-tls-anvil-container",level:3},{value:"Starting the OpenSSL client container",id:"starting-the-openssl-client-container",level:3}],h={toc:p};function m(e){var t=e.components,n=(0,i.Z)(e,l);return(0,a.kt)("wrapper",(0,r.Z)({},h,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"client-testing"},"Client Testing"),(0,a.kt)("p",null,"This site demonstrates how to test the OpenSSL client provided by the TLS-Docker-Library.\nTesting the client in the most simple form roughly takes around 15 minutes. However, this duration can increase to several depending on the strength parameter that that basically defines how often a single test case triggered with different parameters."),(0,a.kt)("h3",{id:"preperations"},"Preperations"),(0,a.kt)("p",null,"Similar to the server test we first create a dedicated docker network that is used by the TLS-Anvil and OpenSSL client container to communicate with each other."),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"docker network create tls-anvil\n")),(0,a.kt)("h3",{id:"starting-the-tls-anvil-container"},"Starting the TLS-Anvil container"),(0,a.kt)("p",null,"Since the client has to connect to TLS-Anvil the test suite container is started first."),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash",metastring:"showLineNumbers",showLineNumbers:!0},"docker run \\\n    --rm \\\n    -it \\\n    -v $(pwd):/output/ \\\n    --network tls-anvil \\\n    --name tls-anvil \\\n    ghcr.io/tls-attacker/tlsanvil:latest \\\n    -outputFolder ./ \\\n    -parallelHandshakes 3 \\\n    -parallelTests 3 \\\n    -strength 1 \\\n    -identifier openssl-client \\\n    client \\\n    -port 8443 \\\n    -triggerScript curl --connect-timeout 2 openssl-client:8090/trigger\n")),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Lines 2-6: Docker related command flags"),(0,a.kt)("li",{parentName:"ul"},"Line 7: Specifies the TLS-Anvil docker image"),(0,a.kt)("li",{parentName:"ul"},"Lines 9-10: Since the client can started multiple times, TLS-Anvil can run multiple tests and handshakes in parallel"),(0,a.kt)("li",{parentName:"ul"},"Line 11: Defines the strength, i.e. the ",(0,a.kt)("inlineCode",{parentName:"li"},"t")," for t-way combinatorial testing"),(0,a.kt)("li",{parentName:"ul"},"Line 12: Defines an arbitrary name that is written to the report"),(0,a.kt)("li",{parentName:"ul"},"Line 13: We want to test a client"),(0,a.kt)("li",{parentName:"ul"},"Line 14: The port on which TLS-Anvil listens to accept requests from the client"),(0,a.kt)("li",{parentName:"ul"},"Line 15: Specifies a script that is executed before each handshake, which the goal to trigger a connection from the client. See below how this works.")),(0,a.kt)("h3",{id:"starting-the-openssl-client-container"},"Starting the OpenSSL client container"),(0,a.kt)("p",null,"The OpenSSL client image is provided by the TLS-Docker-Library. The entrypoint of the client images is a small HTTP server that provides two REST-API endpoints on port 8090."),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"GET /trigger")," starts the client"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"GET /shutdown")," shutdown the HTTP server to terminate the container")),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash",metastring:"showLineNumbers",showLineNumbers:!0},"docker run \\\n    -d \\\n    --rm \\\n    --name openssl-client \\\n    --network tls-anvil \\\n    ghcr.io/tls-attacker/openssl-client:1.1.1i \\\n    -connect tls-anvil:8443\n")),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Lines 2-5: Docker related command flags"),(0,a.kt)("li",{parentName:"ul"},"Line 7: Specifies the OpenSSL client image from the TLS-Docker-Library"),(0,a.kt)("li",{parentName:"ul"},"Line 8: This is passed to the OpenSSL ",(0,a.kt)("inlineCode",{parentName:"li"},"s_client")," binary, which is started each time a HTTP-GET request is sent to ",(0,a.kt)("inlineCode",{parentName:"li"},":8090/trigger"),".")))}m.isMDXComponent=!0}}]);