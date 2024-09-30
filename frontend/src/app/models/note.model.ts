export interface TextNode {
  id: string | null; // This is fine; we'll handle null checks properly in code
  content: string;
}

export interface ImageNode {
  id: string | null; // Same here, we'll manage nulls carefully
  imagePath: string;
}

export interface NoteChild {
  id: string | null;
  position: number;
  textNode?: TextNode | null;
  imageNode?: ImageNode | null;
  type: 'text' | 'image';
}

export interface Note {
  id: string;
  title: string;
  contents: NoteChild[];
  lastInteractedWith: string;
}
