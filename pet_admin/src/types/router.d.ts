import "vue-router"
import type { ManagementRole } from "./user"

declare module "vue-router" {
  interface RouteMeta {
    public?: boolean
    roles?: ManagementRole[]
    title?: string
  }
}
