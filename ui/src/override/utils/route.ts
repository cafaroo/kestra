import {RouteLocationNormalizedLoaded} from "vue-router"

declare global {
    interface Window {
        KESTRA_BASE_PATH: string
    }
}

const ACTIVE_TENANT_STORAGE_KEY = "kestra-active-tenant"

const createBaseUrl = (): string => {
    const root = (import.meta.env.VITE_APP_API_URL || "") + (window.KESTRA_BASE_PATH || "")
    return root.trim() || window.location.origin
}

function storedTenant(): string {
    try {
        return localStorage.getItem(ACTIVE_TENANT_STORAGE_KEY) || "main"
    } catch {
        return "main"
    }
}

let activeTenant = storedTenant()

export const setActiveTenant = (tenant: string | string[] | undefined): void => {
    const value = Array.isArray(tenant) ? tenant[0] : tenant
    if (value) activeTenant = value
}

export const getActiveTenant = (): string => activeTenant

export const baseUrl = createBaseUrl().replace(/\/$/, "")
export const basePath = () => `/api/v1/${activeTenant}`
export const basePathWithoutTenant = () => "/api/v1"

export const apiUrl = (): string => {
    return `${baseUrl}${basePath()}`
}

export const apiUrlWithTenant = (route: RouteLocationNormalizedLoaded): string => {
    const value = route.params.tenant
    const tenant = (Array.isArray(value) ? value[0] : value) || activeTenant
    return `${baseUrl}/api/v1/${tenant}`
}

export const apiUrlWithoutTenants = (): string => `${baseUrl}${basePathWithoutTenant()}`
