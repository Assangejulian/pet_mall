export interface Product {
  id: string;
  productName?: string;
  productType?: number;
  category?: string;
  productDesc?: string;
  detail?: string;
  price: number;
  stock: number;
  status?: number;
  statusCode?: number;
  mainImage?: string;
  image?: string;
  images?: string;
  storeId: string;
  name?: string;  // alias for productName
  videoId?: number;
  createTime: string;
}
