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

export declare const noteTabs: string[]
export declare const notes: Note[]
export declare const tweets: Tweet[]
