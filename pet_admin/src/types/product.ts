export interface Product {
  id: string;
  productName?: string;
  productType?: number;
  category?: string;
  productDesc?: string;
  detail?: string;
  price: number;
  stock: number;
  status?: string | number;
  statusCode?: number;
  mainImage?: string;
  image?: string;
  images?: string;
  storeId: string;
  name?: string;  // alias for productName
  videoId?: number;
  offlineReason?: string;
  offlineUserId?: string;
  offlineTime?: string;
  platformRestricted?: boolean;
  store?: {
    id: string;
    storeName: string;
    status?: number;
    statusText?: string;
  };
  createTime: string;
}
