export interface Tweet {
  user: string
  handle: string
  time: string
  avatar: string
  text: string
  image: string
  stats: [string, string, string]
}

export interface Note {
  title: string
  label: string
  image?: string
  desc: string
  meta: string
}

export declare const communityNav: string[]
export declare const myPets: [string, string][]
export declare const topics: [string, string][]
export declare const tweets: Tweet[]
export declare const ranks: [string, string, string, string][]
export declare const noteTabs: string[]
export declare const notes: Note[]
