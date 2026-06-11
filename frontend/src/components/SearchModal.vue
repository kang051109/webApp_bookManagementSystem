<template>
  <teleport to="body">
    <div class="search-overlay" v-if="visible" @click.self="close">
      <div class="search-box">
        <input ref="input" v-model="q" placeholder="SearchTitle、Author..." @keydown.enter="search" @keydown.esc="close" />
        <div class="search-results" v-if="results.length>0">
          <div v-for="b in results" :key="b.id" class="search-item" @click="go(b.id)">
            <span class="si-title">{{ b.title }}</span>
            <span class="si-author">{{ b.author }}</span>
            <span class="si-cat">{{ b.categoryName||'' }}</span>
          </div>
        </div>
        <div class="search-empty" v-if="searched&&results.length===0">No matching books found</div>
      </div>
    </div>
  </teleport>
</template>

<script>
import api from '../services/api.js'
export default {
  name:'SearchModal',
  data(){return{visible:false,q:'',results:[],searched:false}},
  methods:{
    open(){this.visible=true;this.q='';this.results=[];this.searched=false;this.$nextTick(()=>this.$refs.input?.focus())},
    close(){this.visible=false},
    async search(){
      if(!this.q.trim())return
      this.searched=true
      try{
        const r=await api.get('/books',{params:{keyword:this.q.trim(),size:8}})
        if(r.code===200&&r.data)this.results=r.data.books||[]
      }catch{this.results=[]}
    },
    go(id){this.close();this.$router.push(`/books/${id}`)}
  },
  mounted(){
    window.addEventListener('keydown',e=>{
      if((e.ctrlKey||e.metaKey)&&e.key==='k'){e.preventDefault();this.open()}
      if(e.key==='Escape')this.close()
    })
  }
}
</script>

<style scoped>
.search-overlay{position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:1000;display:flex;align-items:flex-start;justify-content:center;padding-top:15vh}
.search-box{background:var(--surface);border:1px solid var(--warm-border);width:100%;max-width:520px;box-shadow:0 20px 60px rgba(0,0,0,0.2)}
.search-box input{width:100%;padding:16px 20px;border:none;font-size:1rem;background:transparent;color:var(--warm-black);outline:none;font-family:var(--font-body)}
.search-results{max-height:360px;overflow-y:auto;border-top:1px solid var(--warm-border)}
.search-item{display:flex;align-items:center;gap:16px;padding:12px 20px;cursor:pointer;transition:background 0.1s;border-bottom:1px solid var(--warm-border)}
.search-item:hover{background:rgba(196,86,43,0.06)}
.si-title{flex:1;font-weight:600;font-size:0.875rem}
.si-author{color:var(--warm-gray);font-size:0.75rem;min-width:80px}
.si-cat{color:var(--warm-gray-light);font-size:0.6875rem;min-width:60px}
.search-empty{padding:20px;text-align:center;color:var(--warm-gray);font-size:0.875rem}
</style>
