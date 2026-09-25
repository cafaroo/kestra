import type {Router, RouteLocationNormalized, RouteLocationRaw, RouteLocationNamedRaw} from "vue-router"
import type {App} from "vue"
import {getActiveTenant, setActiveTenant} from "override/utils/route"

export function tenantGuard(_router: Router, to: RouteLocationNormalized, from: RouteLocationNormalized): boolean | RouteLocationRaw {
    if (to.meta?.anonymous === true || to.meta?.tenantless === true) {
        return true
    }

    if (to.params.tenant) {
        setActiveTenant(to.params.tenant as string | string[])
        return true
    }

    if (to.path !== "/") {
        const fromTenant = from.params?.tenant
        const currentTenant = (Array.isArray(fromTenant) ? fromTenant[0] : fromTenant) || getActiveTenant()
        return {path: `/${currentTenant}${to.path}`, query: to.query, hash: to.hash}
    }

    return true
}

export function setupTenantRouter(router: Router, app: App): void {
    const originalResolve = router.resolve
    router.resolve = function(to: RouteLocationRaw, currentLocation?: RouteLocationNormalized) {
        if (to && typeof to === "object" && "name" in to && to.name && (!to.params || !to.params.tenant)) {
            to = {...to, params: {tenant: getActiveTenant(), ...to.params}}
        }
        return originalResolve.call(this, to, currentLocation)
    }

    router.afterEach((to) => {
        if (to.params.tenant) setActiveTenant(to.params.tenant as string | string[])
    })

    app.config.globalProperties.$routeTo = function(to: RouteLocationRaw): RouteLocationRaw {
        if (typeof to === "string") {
            return to
        }

        const toWithParams = to as RouteLocationNamedRaw
        return {
            ...toWithParams,
            params: {tenant: this.$route?.params?.tenant || getActiveTenant(), ...toWithParams.params},
        }
    }
}
