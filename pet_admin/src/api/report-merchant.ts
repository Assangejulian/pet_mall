import { merchantHttp } from "./index"
import { unwrap } from "./helper"
import type {
  OrderReportVO,
  ReportQuery,
  SalesTop10ReportVO,
  TurnoverReportVO,
  UserReportVO,
} from "./report"

export async function getMerchantTurnover(params: ReportQuery): Promise<TurnoverReportVO> {
  return unwrap<TurnoverReportVO>(merchantHttp.get("/report/turnover", { params }))
}

export async function getMerchantUsers(params: ReportQuery): Promise<UserReportVO> {
  return unwrap<UserReportVO>(merchantHttp.get("/report/users", { params }))
}

export async function getMerchantOrders(params: ReportQuery): Promise<OrderReportVO> {
  return unwrap<OrderReportVO>(merchantHttp.get("/report/orders", { params }))
}

export async function getMerchantTop10(params: ReportQuery): Promise<SalesTop10ReportVO> {
  return unwrap<SalesTop10ReportVO>(merchantHttp.get("/report/top10", { params }))
}
