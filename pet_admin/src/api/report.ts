import http from "../api/index"
import { unwrap } from "../api/helper"

export interface TurnoverReportVO {
  dateList: string
  turnoverList: string
}

export interface UserReportVO {
  dateList: string
  newUserList: string
  totalUserList: string
}

export interface OrderReportVO {
  dateList: string
  orderCountList: string
  validOrderCountList: string
  completedOrderCountList: string
}

export interface SalesTop10ReportVO {
  nameList: string
  numberList: string
}

export interface ReportQuery {
  begin: string
  end: string
}

export async function getTurnoverReport(params: ReportQuery): Promise<TurnoverReportVO> {
  return unwrap<TurnoverReportVO>(http.get("/report/turnover", { params }))
}

export async function getUserReport(params: ReportQuery): Promise<UserReportVO> {
  return unwrap<UserReportVO>(http.get("/report/users", { params }))
}

export async function getOrderReport(params: ReportQuery): Promise<OrderReportVO> {
  return unwrap<OrderReportVO>(http.get("/report/orders", { params }))
}

export async function getSalesTop10(params: ReportQuery): Promise<SalesTop10ReportVO> {
  return unwrap<SalesTop10ReportVO>(http.get("/report/top10", { params }))
}
