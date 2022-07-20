"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[89],{3905:function(e,t,a){a.d(t,{Zo:function(){return m},kt:function(){return p}});var r=a(7294);function n(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function l(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,r)}return a}function i(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?l(Object(a),!0).forEach((function(t){n(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):l(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function o(e,t){if(null==e)return{};var a,r,n=function(e,t){if(null==e)return{};var a,r,n={},l=Object.keys(e);for(r=0;r<l.length;r++)a=l[r],t.indexOf(a)>=0||(n[a]=e[a]);return n}(e,t);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);for(r=0;r<l.length;r++)a=l[r],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(n[a]=e[a])}return n}var c=r.createContext({}),s=function(e){var t=r.useContext(c),a=t;return e&&(a="function"==typeof e?e(t):i(i({},t),e)),a},m=function(e){var t=s(e.components);return r.createElement(c.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var a=e.components,n=e.mdxType,l=e.originalType,c=e.parentName,m=o(e,["components","mdxType","originalType","parentName"]),d=s(a),p=n,g=d["".concat(c,".").concat(p)]||d[p]||u[p]||l;return a?r.createElement(g,i(i({ref:t},m),{},{components:a})):r.createElement(g,i({ref:t},m))}));function p(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var l=a.length,i=new Array(l);i[0]=d;var o={};for(var c in t)hasOwnProperty.call(t,c)&&(o[c]=t[c]);o.originalType=e,o.mdxType="string"==typeof e?e:n,i[1]=o;for(var s=2;s<l;s++)i[s]=a[s];return r.createElement.apply(null,i)}return r.createElement.apply(null,a)}d.displayName="MDXCreateElement"},4118:function(e,t,a){a.d(t,{Z:function(){return N}});var r=a(3366),n=a(7294),l=a(6010),i=a(8277),o=a(9960),c=a(5999),s="sidebar_TMXw",m="sidebarItemTitle_V4zb",u="sidebarItemList_uHd5",d="sidebarItem_spIe",p="sidebarItemLink_eqrF",g="sidebarItemLinkActive_XZSJ";function f(e){var t=e.sidebar;return n.createElement("aside",{className:"col col--3"},n.createElement("nav",{className:(0,l.Z)(s,"thin-scrollbar"),"aria-label":(0,c.I)({id:"theme.blog.sidebar.navAriaLabel",message:"Blog recent posts navigation",description:"The ARIA label for recent posts in the blog sidebar"})},n.createElement("div",{className:(0,l.Z)(m,"margin-bottom--md")},t.title),n.createElement("ul",{className:(0,l.Z)(u,"clean-list")},t.items.map((function(e){return n.createElement("li",{key:e.permalink,className:d},n.createElement(o.Z,{isNavLink:!0,to:e.permalink,className:p,activeClassName:g},e.title))})))))}var v=a(3102);function h(e){var t=e.sidebar;return n.createElement("ul",{className:"menu__list"},t.items.map((function(e){return n.createElement("li",{key:e.permalink,className:"menu__list-item"},n.createElement(o.Z,{isNavLink:!0,to:e.permalink,className:"menu__link",activeClassName:"menu__link--active"},e.title))})))}function E(e){return n.createElement(v.Zo,{component:h,props:e})}var b=a(7524);function Z(e){var t=e.sidebar,a=(0,b.i)();return null!=t&&t.items.length?"mobile"===a?n.createElement(E,{sidebar:t}):n.createElement(f,{sidebar:t}):null}var y=["sidebar","toc","children"];function N(e){var t=e.sidebar,a=e.toc,o=e.children,c=(0,r.Z)(e,y),s=t&&t.items.length>0;return n.createElement(i.Z,c,n.createElement("div",{className:"container margin-vert--lg"},n.createElement("div",{className:"row"},n.createElement(Z,{sidebar:t}),n.createElement("main",{className:(0,l.Z)("col",{"col--7":s,"col--9 col--offset-1":!s}),itemScope:!0,itemType:"http://schema.org/Blog"},o),a&&n.createElement("div",{className:"col col--2"},a))))}},2754:function(e,t,a){a.r(t),a.d(t,{default:function(){return v}});var r=a(7294),n=a(2263),l=a(4118),i=a(8765),o=a(5999),c=a(1750);function s(e){var t=e.metadata,a=t.previousPage,n=t.nextPage;return r.createElement("nav",{className:"pagination-nav","aria-label":(0,o.I)({id:"theme.blog.paginator.navAriaLabel",message:"Blog list page navigation",description:"The ARIA label for the blog pagination"})},a&&r.createElement(c.Z,{permalink:a,title:r.createElement(o.Z,{id:"theme.blog.paginator.newerEntries",description:"The label used to navigate to the newer blog posts page (previous page)"},"Newer Entries")}),n&&r.createElement(c.Z,{permalink:n,title:r.createElement(o.Z,{id:"theme.blog.paginator.olderEntries",description:"The label used to navigate to the older blog posts page (next page)"},"Older Entries"),isNext:!0}))}var m=a(1944),u=a(5281),d=a(4739),p=a(6010);function g(e){var t=e.metadata,a=(0,n.Z)().siteConfig.title,l=t.blogDescription,i=t.blogTitle,o="/"===t.permalink?a:i;return r.createElement(r.Fragment,null,r.createElement(m.d,{title:o,description:l}),r.createElement(d.Z,{tag:"blog_posts_list"}))}function f(e){var t=e.metadata,a=e.items,n=e.sidebar;return r.createElement(l.Z,{sidebar:n},a.map((function(e){var t=e.content;return r.createElement(i.Z,{key:t.metadata.permalink,frontMatter:t.frontMatter,assets:t.assets,metadata:t.metadata,truncated:t.metadata.truncated},r.createElement(t,null))})),r.createElement(s,{metadata:t}))}function v(e){return r.createElement(m.FG,{className:(0,p.Z)(u.k.wrapper.blogPages,u.k.page.blogListPage)},r.createElement(g,e),r.createElement(f,e))}},8765:function(e,t,a){a.d(t,{Z:function(){return O}});var r=a(7294),n=a(6010),l=a(5999),i=a(9960),o=a(4996),c=a(2263),s=["zero","one","two","few","many","other"];function m(e){return s.filter((function(t){return e.includes(t)}))}var u={locale:"en",pluralForms:m(["one","other"]),select:function(e){return 1===e?"one":"other"}};function d(){var e=(0,c.Z)().i18n.currentLocale;return(0,r.useMemo)((function(){try{return t=e,a=new Intl.PluralRules(t),{locale:t,pluralForms:m(a.resolvedOptions().pluralCategories),select:function(e){return a.select(e)}}}catch(r){return console.error('Failed to use Intl.PluralRules for locale "'+e+'".\nDocusaurus will fallback to the default (English) implementation.\nError: '+r.message+"\n"),u}var t,a}),[e])}function p(){var e=d();return{selectMessage:function(t,a){return function(e,t,a){var r=e.split("|");if(1===r.length)return r[0];r.length>a.pluralForms.length&&console.error("For locale="+a.locale+", a maximum of "+a.pluralForms.length+" plural forms are expected ("+a.pluralForms+"), but the message contains "+r.length+": "+e);var n=a.select(t),l=a.pluralForms.indexOf(n);return r[Math.min(l,r.length-1)]}(a,t,e)}}}var g=a(8780),f=a(6810),v=a(6753),h="blogPostTitle_rzP5",E="blogPostData_Zg1s",b="blogPostDetailsFull_h6_j",Z=a(8727);function y(e){return e.href?r.createElement(i.Z,e):r.createElement(r.Fragment,null,e.children)}function N(e){var t=e.author,a=t.name,n=t.title,l=t.url,i=t.imageURL,o=t.email,c=l||o&&"mailto:"+o||void 0;return r.createElement("div",{className:"avatar margin-bottom--sm"},i&&r.createElement(y,{href:c,className:"avatar__photo-link"},r.createElement("img",{className:"avatar__photo",src:i,alt:a})),a&&r.createElement("div",{className:"avatar__intro",itemProp:"author",itemScope:!0,itemType:"https://schema.org/Person"},r.createElement("div",{className:"avatar__name"},r.createElement(y,{href:c,itemProp:"url"},r.createElement("span",{itemProp:"name"},a))),n&&r.createElement("small",{className:"avatar__subtitle",itemProp:"description"},n)))}var _="authorCol_FlmR",k="imageOnlyAuthorRow_trpF",P="imageOnlyAuthorCol_S2np";function T(e){var t=e.authors,a=e.assets;if(0===t.length)return null;var l=t.every((function(e){return!e.name}));return r.createElement("div",{className:(0,n.Z)("margin-top--md margin-bottom--sm",l?k:"row")},t.map((function(e,t){var i;return r.createElement("div",{className:(0,n.Z)(!l&&"col col--6",l?P:_),key:t},r.createElement(N,{author:Object.assign({},e,{imageURL:null!=(i=a.authorsImageUrls[t])?i:e.imageURL})}))})))}function O(e){var t,a,c=(a=p().selectMessage,function(e){var t=Math.ceil(e);return a(t,(0,l.I)({id:"theme.blog.post.readingTime.plurals",description:'Pluralized label for "{readingTime} min read". Use as much plural forms (separated by "|") as your language support (see https://www.unicode.org/cldr/cldr-aux/charts/34/supplemental/language_plural_rules.html)',message:"One min read|{readingTime} min read"},{readingTime:t}))}),s=(0,o.C)().withBaseUrl,m=e.children,u=e.frontMatter,d=e.assets,y=e.metadata,N=e.truncated,_=e.isBlogPostPage,k=void 0!==_&&_,P=y.date,O=y.formattedDate,w=y.permalink,x=y.tags,C=y.readingTime,j=y.title,D=y.editUrl,F=y.authors,I=null!=(t=d.image)?t:u.image,S=!k&&N,L=x.length>0,R=k?"h1":"h2";return r.createElement("article",{className:k?void 0:"margin-bottom--xl",itemProp:"blogPost",itemScope:!0,itemType:"http://schema.org/BlogPosting"},r.createElement("header",null,r.createElement(R,{className:h,itemProp:"headline"},k?j:r.createElement(i.Z,{itemProp:"url",to:w},j)),r.createElement("div",{className:(0,n.Z)(E,"margin-vert--md")},r.createElement("time",{dateTime:P,itemProp:"datePublished"},O),void 0!==C&&r.createElement(r.Fragment,null," \xb7 ",c(C))),r.createElement(T,{authors:F,assets:d})),I&&r.createElement("meta",{itemProp:"image",content:s(I,{absolute:!0})}),r.createElement("div",{id:k?g.blogPostContainerID:void 0,className:"markdown",itemProp:"articleBody"},r.createElement(f.Z,null,m)),(L||N)&&r.createElement("footer",{className:(0,n.Z)("row docusaurus-mt-lg",k&&b)},L&&r.createElement("div",{className:(0,n.Z)("col",{"col--9":S})},r.createElement(Z.Z,{tags:x})),k&&D&&r.createElement("div",{className:"col margin-top--sm"},r.createElement(v.Z,{editUrl:D})),S&&r.createElement("div",{className:(0,n.Z)("col text--right",{"col--3":L})},r.createElement(i.Z,{to:y.permalink,"aria-label":(0,l.I)({message:"Read more about {title}",id:"theme.blog.post.readMoreLabel",description:"The ARIA label for the link to full blog posts from excerpts"},{title:j})},r.createElement("b",null,r.createElement(l.Z,{id:"theme.blog.post.readMore",description:"The label used in blog post item excerpts to link to full blog posts"},"Read More"))))))}},6753:function(e,t,a){a.d(t,{Z:function(){return d}});var r=a(7294),n=a(5999),l=a(7462),i=a(3366),o=a(6010),c="iconEdit_dcUD",s=["className"];function m(e){var t=e.className,a=(0,i.Z)(e,s);return r.createElement("svg",(0,l.Z)({fill:"currentColor",height:"20",width:"20",viewBox:"0 0 40 40",className:(0,o.Z)(c,t),"aria-hidden":"true"},a),r.createElement("g",null,r.createElement("path",{d:"m34.5 11.7l-3 3.1-6.3-6.3 3.1-3q0.5-0.5 1.2-0.5t1.1 0.5l3.9 3.9q0.5 0.4 0.5 1.1t-0.5 1.2z m-29.5 17.1l18.4-18.5 6.3 6.3-18.4 18.4h-6.3v-6.2z"})))}var u=a(5281);function d(e){var t=e.editUrl;return r.createElement("a",{href:t,target:"_blank",rel:"noreferrer noopener",className:u.k.common.editThisPage},r.createElement(m,null),r.createElement(n.Z,{id:"theme.common.editThisPage",description:"The link label to edit the current page"},"Edit this page"))}},9649:function(e,t,a){a.d(t,{Z:function(){return d}});var r=a(7462),n=a(3366),l=a(7294),i=a(6010),o=a(5999),c=a(6668),s="anchorWithStickyNavbar_mojV",m="anchorWithHideOnScrollNavbar_R0VQ",u=["as","id"];function d(e){var t=e.as,a=e.id,d=(0,n.Z)(e,u),p=(0,c.L)().navbar.hideOnScroll;return"h1"!==t&&a?l.createElement(t,(0,r.Z)({},d,{className:(0,i.Z)("anchor",p?m:s),id:a}),d.children,l.createElement("a",{className:"hash-link",href:"#"+a,title:(0,o.I)({id:"theme.common.headingLinkTitle",message:"Direct link to heading",description:"Title for link to heading"})},"\u200b")):l.createElement(t,(0,r.Z)({},d,{id:void 0}))}},6810:function(e,t,a){a.d(t,{Z:function(){return O}});var r=a(7294),n=a(3905),l=a(7462),i=a(3366),o=a(5742),c=["mdxType","originalType"];var s=a(5710);var m=a(9960);var u=a(6010),d=a(2389),p=a(6043),g="details_lb9f",f="isBrowser_bmU9",v="collapsibleContent_i85q",h=["summary","children"];function E(e){return!!e&&("SUMMARY"===e.tagName||E(e.parentElement))}function b(e,t){return!!e&&(e===t||b(e.parentElement,t))}function Z(e){var t=e.summary,a=e.children,n=(0,i.Z)(e,h),l=(0,d.Z)(),o=(0,r.useRef)(null),c=(0,p.u)({initialState:!n.open}),s=c.collapsed,m=c.setCollapsed,Z=(0,r.useState)(n.open),y=Z[0],N=Z[1];return r.createElement("details",Object.assign({},n,{ref:o,open:y,"data-collapsed":s,className:(0,u.Z)(g,l&&f,n.className),onMouseDown:function(e){E(e.target)&&e.detail>1&&e.preventDefault()},onClick:function(e){e.stopPropagation();var t=e.target;E(t)&&b(t,o.current)&&(e.preventDefault(),s?(m(!1),N(!0)):m(!0))}}),t||r.createElement("summary",null,"Details"),r.createElement(p.z,{lazy:!1,collapsed:s,disableSSRStyle:!0,onCollapseTransitionEnd:function(e){m(e),N(!e)}},r.createElement("div",{className:v},a)))}var y="details_BAp3";function N(e){var t=Object.assign({},e);return r.createElement(Z,(0,l.Z)({},t,{className:(0,u.Z)("alert alert--info",y,t.className)}))}var _=a(9649);function k(e){return r.createElement(_.Z,e)}var P="img_E7b_";var T={head:function(e){var t=r.Children.map(e.children,(function(e){return function(e){var t,a;if(null!=e&&null!=(t=e.props)&&t.mdxType&&null!=e&&null!=(a=e.props)&&a.originalType){var n=e.props,l=(n.mdxType,n.originalType,(0,i.Z)(n,c));return r.createElement(e.props.originalType,l)}return e}(e)}));return r.createElement(o.Z,e,t)},code:function(e){var t=["a","b","big","i","span","em","strong","sup","sub","small"];return r.Children.toArray(e.children).every((function(e){return"string"==typeof e&&!e.includes("\n")||(0,r.isValidElement)(e)&&t.includes(e.props.mdxType)}))?r.createElement("code",e):r.createElement(s.Z,e)},a:function(e){return r.createElement(m.Z,e)},pre:function(e){var t;return r.createElement(s.Z,(0,r.isValidElement)(e.children)&&"code"===e.children.props.originalType?null==(t=e.children)?void 0:t.props:Object.assign({},e))},details:function(e){var t=r.Children.toArray(e.children),a=t.find((function(e){var t;return"summary"===(null==e||null==(t=e.props)?void 0:t.mdxType)})),n=r.createElement(r.Fragment,null,t.filter((function(e){return e!==a})));return r.createElement(N,(0,l.Z)({},e,{summary:a}),n)},ul:function(e){return r.createElement("ul",(0,l.Z)({},e,{className:(t=e.className,(0,u.Z)(t,(null==t?void 0:t.includes("contains-task-list"))&&"clean-list"))}));var t},img:function(e){return r.createElement("img",(0,l.Z)({loading:"lazy"},e,{className:(t=e.className,(0,u.Z)(t,P))}));var t},h1:function(e){return r.createElement(k,(0,l.Z)({as:"h1"},e))},h2:function(e){return r.createElement(k,(0,l.Z)({as:"h2"},e))},h3:function(e){return r.createElement(k,(0,l.Z)({as:"h3"},e))},h4:function(e){return r.createElement(k,(0,l.Z)({as:"h4"},e))},h5:function(e){return r.createElement(k,(0,l.Z)({as:"h5"},e))},h6:function(e){return r.createElement(k,(0,l.Z)({as:"h6"},e))}};function O(e){var t=e.children;return r.createElement(n.Zo,{components:T},t)}},1750:function(e,t,a){a.d(t,{Z:function(){return i}});var r=a(7294),n=a(6010),l=a(9960);function i(e){var t=e.permalink,a=e.title,i=e.subLabel,o=e.isNext;return r.createElement(l.Z,{className:(0,n.Z)("pagination-nav__link",o?"pagination-nav__link--next":"pagination-nav__link--prev"),to:t},i&&r.createElement("div",{className:"pagination-nav__sublabel"},i),r.createElement("div",{className:"pagination-nav__label"},a))}},8727:function(e,t,a){a.d(t,{Z:function(){return p}});var r=a(7294),n=a(6010),l=a(5999),i=a(9960),o="tag_hD8n",c="tagRegular_D6E_",s="tagWithCount_i0QQ";function m(e){var t=e.permalink,a=e.label,l=e.count;return r.createElement(i.Z,{href:t,className:(0,n.Z)(o,l?s:c)},a,l&&r.createElement("span",null,l))}var u="tags_XVD_",d="tag_JSN8";function p(e){var t=e.tags;return r.createElement(r.Fragment,null,r.createElement("b",null,r.createElement(l.Z,{id:"theme.tags.tagsListLabel",description:"The label alongside a tag list"},"Tags:")),r.createElement("ul",{className:(0,n.Z)(u,"padding--none","margin-left--sm")},t.map((function(e){var t=e.label,a=e.permalink;return r.createElement("li",{key:a,className:d},r.createElement(m,{label:t,permalink:a}))}))))}}}]);